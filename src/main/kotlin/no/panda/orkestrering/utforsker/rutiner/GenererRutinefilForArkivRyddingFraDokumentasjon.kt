package no.panda.orkestrering.utforsker.rutiner

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import tools.jackson.core.JsonGenerator
import tools.jackson.core.util.DefaultIndenter
import tools.jackson.core.util.DefaultPrettyPrinter
import tools.jackson.databind.ObjectMapper
import java.io.File
import java.nio.file.Files
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

fun main() {

    /*
    Leser driftsdokumentasjon fra https://wiki.spk.no/spaces/dok/pages/537887546/SPK-Panda+Driftsdokumentasjon+for+automatisk+sletting+av+arkiverte+filer
     */
    val client: OkHttpClient = spkSignertKlient()

    val request: Request = Request.Builder()
        .url("https://wiki.spk.no/rest/api/content/537887546?expand=body.storage")
        .build()

    val tekst = try {
        client.newCall(request).execute().use { resp ->
            if (!resp.isSuccessful) throw RuntimeException("HTTP ${resp.code}")
            resp.body.string()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return
    }


    val jsonNode = ObjectMapper().readTree(tekst)
    val innhold = jsonNode.get("body").get("storage").get("value").asText()

    val grupperMedHandlinger = parseDokumentasjonForSletteInnslag(innhold)

    val jsondokument = konverterGrupperTilRutinefil(grupperMedHandlinger)

    val tekstdokument = ObjectMapper()
        .writer()
        .with(IntellijPrettyPrinter())
        .writeValueAsString(jsondokument)
    Files.write(File("maler/system/system_maanedlig_rydding_arkiv.json").toPath(), tekstdokument.toByteArray(Charsets.UTF_8))
}

fun spkSignertKlient(): OkHttpClient {
    try {
        // Last inn CA-sertifikat fra resources
        val cf = CertificateFactory.getInstance("X.509")
        val caInput = Thread.currentThread().contextClassLoader.getResourceAsStream("spk_root_ca.crt")
            ?: throw RuntimeException("Fant ikke spk_root_ca.crt i resources")
        caInput.use {
            val ca = cf.generateCertificate(caInput)

            // Opprett en KeyStore med CA
            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setCertificateEntry("spk_root_ca", ca)

            // Opprett TrustManagerFactory med KeyStore
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(keyStore)

            // Opprett SSLContext med TrustManager
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, tmf.getTrustManagers(), SecureRandom())

            val client: OkHttpClient = OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), (tmf.getTrustManagers()[0] as X509TrustManager?)!!)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            return client
        }
    } catch (e: Exception) {
        throw RuntimeException(e)
    }
}

private fun konverterGrupperTilRutinefil(grupperMedHandlinger: ArrayList<GruppeAvHandlinger>): Map<String, Any> {
    val operasjoner = ArrayList<Map<String, Any>>()
    grupperMedHandlinger.forEachIndexed { index, gruppe ->
        val operasjon = mapOf(
            "navn" to gruppe.navn,
            "handling" to "gruppe",
            "gruppeAv" to gruppe.gruppe.map {
                mapOf(
                    "handling" to "slett_foreldede_filer_arkiv",
                    "arkivområde" to it.filsti.replace("SPK_DATA/panda/arkiv/", ""),
                    "maksLevetidDager" to if (it.ignoreresPgaEvigLevetid == true) "" else "\${${
                        it.levetid.replace(
                            " ",
                            ""
                        ).lowercase()
                    }}",
                    "testkjøring" to "\${erTestkjøring}",
                    "ignorertInstruksjon" to it.ignoreresPgaEvigLevetid
                )
            }
        )
        operasjoner.add(operasjon)
    }

    val json = mapOf(
        "navn" to "Månedlig rydding arkiv",
        "metainfo" to mapOf(
            "mal" to "maler/system/system_maanedlig_rydding_arkiv.json",
            "tags" to listOf("vedlikehold"),
            "støttetAvPaOrkBa01FraVersjon" to "1.1.3"
        ),
        "variabler" to mapOf(
            "3mnd" to "90",
            "2år" to "730",
            "5år" to "1825",
            "10år" to "3650",
            "erTestkjøring" to "false"
        ),
        "operasjoner" to operasjoner
    )
    return json
}

private fun parseDokumentasjonForSletteInnslag(innhold: String): ArrayList<GruppeAvHandlinger> {
    val doc = Jsoup.parse(innhold)

    val sectionsWithTables = doc.select("h1, h2, h3, h4, h5, h6").flatMap { section ->
        val tables = section.nextElementSiblings()
            .takeWhile { it.tagName() !in listOf("h1", "h2", "h3", "h4", "h5", "h6") }
            .filter { it.tagName() == "table" }
        tables.map { table -> section to table }
    }

    val grupper = ArrayList<GruppeAvHandlinger>()

    for ((header, table) in sectionsWithTables) {
        val handlinger = ArrayList<SletteHandling>()
        val rows = table.select("tr")
        var levetid = ""
        var filsti = ""
        for (row in rows) {
            val cells = row.select("td")
            if (cells.size >= 2) {
                val key = cells[0].text().trim()
                val value = cells[1].text().trim()
                if (key == "Levetid") {
                    levetid = value
                }
                if (key == "Filsti") {
                    filsti = value
                }
            }
        }

        handlinger.add(SletteHandling(filsti, levetid, levetid.equals("Evig", ignoreCase = true)))
        grupper.add(GruppeAvHandlinger(header.text(), handlinger))
    }
    return grupper
}

data class GruppeAvHandlinger(val navn: String, val gruppe: List<SletteHandling>)

data class SletteHandling(val filsti: String, val levetid: String, val ignoreresPgaEvigLevetid: Boolean)

class IntellijPrettyPrinter : DefaultPrettyPrinter() {
    init {
        indentObjectsWith(DefaultIndenter("  ", DefaultIndenter.SYS_LF))
        indentArraysWith(DefaultIndenter("  ", DefaultIndenter.SYS_LF))
    }

    override fun writeObjectNameValueSeparator(g: JsonGenerator) {
        g.writeRaw(": ")
    }

    override fun createInstance(): DefaultPrettyPrinter {
        return IntellijPrettyPrinter()
    }
}