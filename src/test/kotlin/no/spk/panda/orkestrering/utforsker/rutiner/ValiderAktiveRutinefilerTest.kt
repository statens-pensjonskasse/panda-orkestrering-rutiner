package no.spk.panda.orkestrering.utforsker.rutiner

import org.assertj.core.api.Assertions.assertThat
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
            .`as`("Ved validering av ${fil.path}")
            .hasSize(0)
    }
    private fun hbfAsAsaMaler(): Stream<File> {
        val katalog = File("maler/fakturering/hbf_as_asa")
        return hentAlleJsonFiler(katalog).stream()
    }

    @ParameterizedTest
    @MethodSource("hbfFlsMaler")
    fun `skal validere alle maler for HBF FLS`(fil: File) {
        assertThat(validerJsonSkjema(rutinefilSkjema, fil.readText()))
            .`as`("Ved validering av ${fil.path}")
            .hasSize(0)
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
        val katalog = File("maler/fakturering/hbf_ovrige")
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
    @MethodSource("poaAfpMaler")
    fun `skal validere alle maler for POA AFP`(fil: File) {
        assertThat(validerJsonSkjema(rutinefilSkjema, fil.readText()))
            .`as`("Ved validering av ${fil.path}")
            .hasSize(0)
    }

    private fun poaAfpMaler(): Stream<File> {
        val frittstaaende = File("maler/fakturering/poa_frittstaaende")
        val kjeder = File("maler/fakturering/poa_kjeder")
        return Stream.concat(
            hentAlleJsonFiler(frittstaaende).stream(),
            hentAlleJsonFiler(kjeder).stream()
        )
    }

    @ParameterizedTest
    @MethodSource("tjenestemannsorgMaler")
    fun `skal validere alle maler for tjenestemannsorg`(fil: File) {
        assertThat(validerJsonSkjema(rutinefilSkjema, fil.readText()))
            .`as`("Ved validering av ${fil.path}")
            .hasSize(0)
    }

    private fun tjenestemannsorgMaler(): Stream<File> {
        val tjenestemannsorg = File("maler/fakturering/tjenestemannsorg")
        val tjenestemannsorgArbeidsgiverandel = File("maler/fakturering/tjenestemannsorg_arbeidsgiverandel")
        return Stream.concat(
            hentAlleJsonFiler(tjenestemannsorg).stream(),
            hentAlleJsonFiler(tjenestemannsorgArbeidsgiverandel).stream()
        )
    }

    @ParameterizedTest
    @MethodSource("poaPenMaler")
    fun `skal validere alle maler for POA PEN`(fil: File) {
        assertThat(validerJsonSkjema(rutinefilSkjema, fil.readText()))
            .`as`("Ved validering av ${fil.path}")
            .hasSize(0)
    }

    private fun poaPenMaler(): Stream<File> {
        val katalog = File("maler/fakturering/poa_pen")
        return hentAlleJsonFiler(katalog).stream()
    }

    @ParameterizedTest
    @MethodSource("adHocMaler")
    fun `skal validere alle maler for Ad Hoc`(fil: File) {
        assertThat(validerJsonSkjema(rutinefilSkjema, fil.readText()))
            .`as`("Ved validering av ${fil.path}")
            .hasSize(0)
    }

    private fun adHocMaler(): Stream<File> {
        val katalog = File("maler/fakturering/ad_hoc")
        return hentAlleJsonFiler(katalog).stream()
    }

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
        val månedskjøring = File("maler/fakturering/maanedlig_analyseunderlag_og_premiedifferanser.json")

        assertThat(validerJsonSkjema(rutinefilSkjema, månedskjøring.readText()))
            .`as`("Ved validering av ${månedskjøring.path}")
            .hasSize(0)
    }

    @Test
    fun `skal validere systemkorreksjon`() {
        val månedskjøring = File("maler/fakturering/systemkorreksjon.json")

        assertThat(validerJsonSkjema(rutinefilSkjema, månedskjøring.readText()))
            .`as`("Ved validering av ${månedskjøring.path}")
            .hasSize(0)
    }

}
