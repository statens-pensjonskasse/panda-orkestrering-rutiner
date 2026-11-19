package no.spk.panda.orkestrering.utforsker.rutiner

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.io.File
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValiderAktiveRutinefilerTest {


    val rutinefilSkjema = this::class.java.classLoader.getResource("skjemavalidering/rutinefilSkjema.json")?.readText()
        ?: throw RuntimeException("skjemavalidering/rutinefilSkjema not found")

    // Kataloger og filer under maler som bevisst ikke valideres mot rutinefilskjemaet
    private val ikkeValiderteStier = listOf(
        "maler/generer_md5_filer_for_dvh_ved_endret_antall_kolonner.json",
        "maler/fakturering/rutinefiler_for_test",
    )

    @ParameterizedTest
    @MethodSource("alleMaler")
    fun `skal validere alle maler`(fil: File) {
        val feil = validerJsonSkjema(rutinefilSkjema, fil.readText())

        assertThat(feil)
            .withFailMessage {
                "${fil.path} har ${feil.size} feil mot rutinefilskjemaet:\n" +
                    feil.joinToString("\n") { "  - ${it.beskrivelse()}" }
            }
            .isEmpty()
    }

    // Variabler som bevisst har en fast verdi, og derfor ikke skal fylles ut før bestilling
    private val variablerMedFastVerdi = mapOf(
        "maler/reserveberegning/metaopplaster_aggreger_feilmeldinger_reserveberegning.json" to setOf("databaseKilde"),
        "maler/fakturering/poa_pen/T1-T4/poa_pen_fakturering.json" to setOf("sluttfakturaAvtalelister"),
        "maler/system/system_maanedlig_rydding_arkiv.json" to setOf("3mnd", "2år", "5år", "10år", "erTestkjøring"),
    )

    // Flytkontroll tolker verdier som inneholder <xxx> som ikke utfylt, og hindrer bestilling til de er fylt ut.
    // Verdier på formen #{...} er funksjoner som fylles ut automatisk, og skal derfor ikke ha <xxx>.
    @ParameterizedTest
    @MethodSource("alleMaler")
    fun `skal ha plassholder for alle variabler som må fylles ut`(fil: File) {
        val variabler = ObjectMapper().readTree(fil).path("variabler")
        val unntak = variablerMedFastVerdi[fil.path].orEmpty()

        val variablerUtenPlassholder = variabler.properties()
            .filterNot { (navn, _) -> navn in unntak }
            .filterNot { (_, verdi) -> erPlassholderEllerFunksjon(verdi) }
            .map { (navn, verdi) -> "$navn: $verdi" }

        assertThat(variablerUtenPlassholder)
            .withFailMessage {
                "${fil.path} har variabler uten plassholder på formen <xxx>. " +
                    "Bruk <xxx> for verdier som skal fylles ut, eller legg variabelen til i variablerMedFastVerdi:\n" +
                    variablerUtenPlassholder.joinToString("\n") { "  - $it" }
            }
            .isEmpty()
    }

    private fun erPlassholderEllerFunksjon(verdi: JsonNode): Boolean =
        if (verdi.isArray) {
            !verdi.isEmpty && verdi.all { erPlassholderEllerFunksjon(it) }
        } else {
            val tekst = verdi.stringValue()
            // Tekst rundt plassholderne er lov, men alle < og > må høre til en plassholder <xxx>
            tekst.matches(Regex("""^#\{.+}$""")) || tekst.matches(Regex("^([^<>]*<[^<>]+>)+[^<>]*$"))
        }

    private fun alleMaler(): Stream<File> {
        return hentAlleJsonFiler(File("maler"))
            .filterNot { fil -> ikkeValiderteStier.any { fil.path == it || fil.path.startsWith("$it/") } }
            .stream()
    }
}
