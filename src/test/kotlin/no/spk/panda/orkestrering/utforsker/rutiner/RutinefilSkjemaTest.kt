package no.spk.panda.orkestrering.utforsker.rutiner

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RutinefilSkjemaTest {

    val rutinefilSkjema = this::class.java.classLoader.getResource("skjemavalidering/rutinefilSkjema.json")?.readText()
        ?: throw RuntimeException("skjemavalidering/rutinefilSkjema not found")

    @Test
    fun `Skjemavalidering skal feile når påkrevde egenskaper mangler`() {

        val ingenEgenskaper = """
             {
                
             }
        """.trimIndent()

        assertThat(
            validerJsonSkjema(rutinefilSkjema, ingenEgenskaper)
        )
            .hasSize(3)
            .containsExactlyInAnyOrder(
                SkjemaValidering("required", "$: required property 'navn' not found"),
                SkjemaValidering("required", "$: required property 'variabler' not found"),
                SkjemaValidering("required", "$: required property 'operasjoner' not found"),
            )

    }

    @Test
    fun `Operasjoner må inneholde en eller flere operasjoner`() {

        val ingenEgenskaper = """
             {
                "navn": "En test",
                "variabler": {
                    "enVariabel": "abc"
                },
                "operasjoner": [
                
                ]
             }
        """.trimIndent()

        assertThat(
            validerJsonSkjema(rutinefilSkjema, ingenEgenskaper)
        )
            .hasSize(1)
            .containsExactlyInAnyOrder(
                SkjemaValidering("minItems", "\$.operasjoner: must have at least 1 items but found 0"),
            )

    }

    @Test
    fun `En operasjon skal alltid ha egenskapen handling`() {

        val ingenEgenskaper = """
             {
                "navn": "En test",
                "variabler": {
                    "enVariabel": "abc"
                },
                "operasjoner": [
                    {
                      "batch": "pa_res_ba_01"
                    }
                ]
             }
        """.trimIndent()

        assertThat(
            validerJsonSkjema(rutinefilSkjema, ingenEgenskaper)
        )
            .hasSize(1)
            .containsExactlyInAnyOrder(
                SkjemaValidering("required", "\$.operasjoner[0]: required property 'handling' not found"),
            )

    }

    @Test
    fun `En gyldig rutinefil`() {

        val ingenEgenskaper = """
             {
                "navn": "En gyldig rutinefil",
                "variabler": {
                    "enVariabel": "abc"
                },
                "operasjoner": [
                    {
                      "handling": "pa_res_ba_01"
                    }
                ]
             }
        """.trimIndent()

        assertThat(
            validerJsonSkjema(rutinefilSkjema, ingenEgenskaper)
        )
            .hasSize(0)

    }

}