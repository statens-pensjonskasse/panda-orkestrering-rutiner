package no.spk.panda.orkestrering.utforsker.rutiner

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValiderMalstiTest {

    @ParameterizedTest
    @MethodSource("alleMaler")
    fun `skal verifisere at mal er faktisk filsti for malen`(fil: File) {
        val forventetSti = fil.path

        val om = ObjectMapper()
        val readTree = om.readTree(fil)

        val malNode = readTree.path("metainfo").path("mal")
        assertThat(malNode.isMissingNode).isFalse()
        assertThat(malNode.textValue())
            .withFailMessage("Mal i fil %s var %s, forventet %s", fil.name, malNode.textValue(), forventetSti)
            .isEqualTo(forventetSti)
    }

    private fun alleMaler(): Stream<File> {
        val katalog = File("maler")
        return hentAlleJsonFiler(katalog).stream()
    }

}