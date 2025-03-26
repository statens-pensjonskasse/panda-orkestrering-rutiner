package no.spk.panda.orkestrering.utforsker.rutiner

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.Ignore

class ValiderAktiveRutinefilerTest {


    val rutinefilSkjema = this::class.java.classLoader.getResource("skjemavalidering/rutinefilSkjema.json")?.readText()
        ?: throw RuntimeException("skjemavalidering/rutinefilSkjema not found")

    @Test
    fun `skal validere alle maler for HBF AS ASA`() {
        val katalog = File("maler/fakturering/hbf_as_asa")
        val filer = hentAlleJsonFiler(katalog)

        filer.forEach {
            assertThat(validerJsonSkjema(rutinefilSkjema, it.readText()))
                .hasSize(0)
                .`as`("Ved validering av ${it.path}")
        }
    }

    @Test
    fun `skal validere alle maler for HBF FLS`() {
        val katalog = File("maler/fakturering/hbf_fls")
        val filer = hentAlleJsonFiler(katalog)

        filer.forEach {
            assertThat(validerJsonSkjema(rutinefilSkjema, it.readText()))
                .hasSize(0)
                .`as`("Ved validering av ${it.path}")
        }
    }

    @Test
    fun `skal validere alle maler for HBF Øvrige`() {
        val katalog = File("maler/fakturering/hbf_ovrige")
        val filer = hentAlleJsonFiler(katalog)

        filer.forEach {
            assertThat(validerJsonSkjema(rutinefilSkjema, it.readText()))
                .hasSize(0)
                .`as`("Ved validering av ${it.path}")
        }
    }

    @Test
    fun `skal validere alle maler for HBF Stat`() {
        val katalog = File("maler/fakturering/hbf_stat")
        val filer = hentAlleJsonFiler(katalog)

        filer.forEach {
            assertThat(validerJsonSkjema(rutinefilSkjema, it.readText()))
                .`as`("Ved validering av ${it.path}")
                .hasSize(0)
        }
    }

    @Test
    fun `skal validere alle maler for POA AFP`() {
        val katalog = File("maler/fakturering/hbf_stat")
        val filer = hentAlleJsonFiler(katalog)

        filer.forEach {
            assertThat(validerJsonSkjema(rutinefilSkjema, it.readText()))
                .`as`("Ved validering av ${it.path}")
                .hasSize(0)
        }
    }

    @Ignore
    @Test
    fun `skal validere alle maler for reserveberegningen`() {
        val katalog = File("maler/reserveberegning")
        val filer = hentAlleJsonFiler(katalog)

        filer.forEach {
            assertThat(validerJsonSkjema(rutinefilSkjema, it.readText()))
                .`as`("Ved validering av ${it.path}")
                .hasSize(0)
        }
    }

    @Test
    fun `skal validere månedskjøringen`() {
        val månedskjøring = File("maler/fakturering/templat_maanedlig_analyseunderlag_og_premiedifferanser.json")

        assertThat(validerJsonSkjema(rutinefilSkjema, månedskjøring.readText()))
            .`as`("Ved validering av ${månedskjøring.path}")
            .hasSize(0)

    }

    @Test
    fun `skal validere system korrektsjon`() {
        val månedskjøring = File("maler/fakturering/template_systemkorreksjon.json")

        assertThat(validerJsonSkjema(rutinefilSkjema, månedskjøring.readText()))
            .`as`("Ved validering av ${månedskjøring.path}")
            .hasSize(0)
    }

}
