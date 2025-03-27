package no.spk.panda.orkestrering.utforsker.rutiner

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValiderAktiveRutinefilerTest {


    val rutinefilSkjema = this::class.java.classLoader.getResource("skjemavalidering/rutinefilSkjema.json")?.readText()
        ?: throw RuntimeException("skjemavalidering/rutinefilSkjema not found")

    @ParameterizedTest
    @MethodSource("hbfAsAsaMaler")
    fun `skal validere alle maler for HBF AS ASA`(fil: File) {
        assertThat(validerJsonSkjema(rutinefilSkjema, fil.readText()))
            .hasSize(0)
            .`as`("Ved validering av ${fil.path}")
    }
    private fun hbfAsAsaMaler(): Stream<File> {
        val katalog = File("maler/fakturering/hbf_as_asa")
        return hentAlleJsonFiler(katalog).stream()
    }

    @ParameterizedTest
    @MethodSource("hbfFlsMaler")
    fun `skal validere alle maler for HBF FLS`(fil: File) {
        assertThat(validerJsonSkjema(rutinefilSkjema, fil.readText()))
            .hasSize(0)
            .`as`("Ved validering av ${fil.path}")
    }
    private fun hbfFlsMaler(): Stream<File> {
        val katalog = File("maler/fakturering/hbf_fls_2_0")
        return hentAlleJsonFiler(katalog).stream()
    }

    @ParameterizedTest
    @MethodSource("hbfØvrigeMaler")
    fun `skal validere alle maler for HBF Øvrige`(fil: File) {
        assertThat(validerJsonSkjema(rutinefilSkjema, fil.readText()))
            .`as`("Ved validering av ${fil.path}")
            .hasSize(0)
    }

    private fun hbfØvrigeMaler(): Stream<File> {
        val katalog = File("maler/fakturering/hbf_overige")
        return hentAlleJsonFiler(katalog).stream()
    }


    @ParameterizedTest
    @MethodSource("hbfStatMaler")
    fun `skal validere alle maler for HBF Stat`(fil: File) {
        assertThat(validerJsonSkjema(rutinefilSkjema, fil.readText()))
            .`as`("Ved validering av ${fil.path}")
            .hasSize(0)
    }

    private fun hbfStatMaler(): Stream<File> {
        val katalog = File("maler/fakturering/hbf_stat")
        return hentAlleJsonFiler(katalog).stream()
    }

    @ParameterizedTest
    @MethodSource("poaPenMaler")
    fun `skal validere alle maler for POA AFP`(fil: File) {
        assertThat(validerJsonSkjema(rutinefilSkjema, fil.readText()))
            .`as`("Ved validering av ${fil.path}")
            .hasSize(0)
    }

    private fun poaPenMaler(): Stream<File> {
        val katalog = File("maler/fakturering/poa_pen")
        return hentAlleJsonFiler(katalog).stream()
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("reserveberegningsMaler")
    fun `skal validere alle maler for reserveberegningen`(fil: File) {
        assertThat(validerJsonSkjema(rutinefilSkjema, fil.readText()))
            .`as`("Ved validering av ${fil.path}")
            .hasSize(0)
    }

    private fun reserveberegningsMaler(): Stream<File> {
        val katalog = File("maler/reserveberegning")
        return hentAlleJsonFiler(katalog).stream()
    }


    @Test
    fun `skal validere månedskjøringen`() {
        val månedskjøring = File("maler/fakturering/templat_maanedlig_analyseunderlag_og_premiedifferanser.json")

        assertThat(validerJsonSkjema(rutinefilSkjema, månedskjøring.readText()))
            .`as`("Ved validering av ${månedskjøring.path}")
            .hasSize(0)

    }

    @Test
    fun `skal validere system korreksjon`() {
        val månedskjøring = File("maler/fakturering/template_systemkorreksjon.json")

        assertThat(validerJsonSkjema(rutinefilSkjema, månedskjøring.readText()))
            .`as`("Ved validering av ${månedskjøring.path}")
            .hasSize(0)
    }

}
