package no.panda.orkestrering.utforsker.rutiner

import com.fasterxml.jackson.databind.ObjectMapper
import org.jsoup.Jsoup
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

fun main() {

    val client = enHttpklientSomIgnorererSSLVerifiseringen()

    /*
    Leser driftsdokumentasjon fra https://wiki.spk.no/spaces/dok/pages/537887546/SPK-Panda+Driftsdokumentasjon+for+automatisk+sletting+av+arkiverte+filer
     */
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://wiki.spk.no/rest/api/content/537887546?expand=body.storage"))
        .GET()
        .build()

    val response = client!!.send(request, HttpResponse.BodyHandlers.ofString())
    val jsonNode = ObjectMapper().readTree(response.body())
    val innhold = jsonNode.get("body").get("storage").get("value").asText()

    val grupperMedHandlinger = parseDokumentasjonForSletteInnslag(innhold)

    val jsondokument = konverterGrupperTilRutinefil(grupperMedHandlinger)

    val tekstdokument = ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsondokument)
    Files.write(File("maler/system/system_maanedlig_rydding_arkiv.json").toPath(), tekstdokument.toByteArray(Charsets.UTF_8))
}

private fun enHttpklientSomIgnorererSSLVerifiseringen(): HttpClient? = HttpClient.newBuilder()
    .sslContext(SSLContext.getInstance("TLS").apply {
        init(null, arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }), SecureRandom())
    })
    .build()

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