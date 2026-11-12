package no.spk.panda.orkestrering.utforsker.rutiner

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
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
                    feil.joinToString("\n") { "  - ${it.melding}" }
            }
            .isEmpty()
    }

    private fun alleMaler(): Stream<File> {
        return hentAlleJsonFiler(File("maler"))
            .filterNot { fil -> ikkeValiderteStier.any { fil.path == it || fil.path.startsWith("$it/") } }
            .stream()
    }
}
