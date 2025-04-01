package no.spk.panda.orkestrering.utforsker.rutiner

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
            .hasSize(4)
            .containsExactlyInAnyOrder(
                SkjemaValidering("required", "$: required property 'navn' not found"),
                SkjemaValidering("required", "$: required property 'metainfo' not found"),
                SkjemaValidering("required", "$: required property 'variabler' not found"),
                SkjemaValidering("required", "$: required property 'operasjoner' not found"),
            )

    }

    @Test
    fun `Operasjoner må inneholde en eller flere operasjoner`() {

        val ingenEgenskaper = """
             {
                "navn": "En test",
                "metainfo": {
                    "mal": "maler/rutinefil.json",
                    "tags": ["eksport"],
                    "støttetAvPaOrkBa01FraVersjon": "0.1.8"
                 },
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
    fun `Metainfo har minimum en tag`() {

        val ingenEgenskaper = """
             {
                "navn": "En test",
                "metainfo": {
                    "mal": "maler/rutinefil.json",
                    "tags": [],
                    "støttetAvPaOrkBa01FraVersjon": "0.1.8"
                 },
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
            .hasSize(1)
            .containsExactlyInAnyOrder(
                SkjemaValidering("minItems", "\$.metainfo.tags: must have at least 1 items but found 0"),
            )

    }


    @ParameterizedTest
    @MethodSource("tagsSomKreverPremiemodell")
    fun `Metainfo tagget med fakturering, prognose og agresso skal kreve premiemodell`(tag: String) {

        val ingenEgenskaper = """
             {
                "navn": "En test",
                "metainfo": {
                    "mal": "maler/rutinefil.json",
                    "tags": ["$tag"],
                    "støttetAvPaOrkBa01FraVersjon": "0.1.8"
                 },
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
            .hasSize(1)
            .containsExactlyInAnyOrder(
                SkjemaValidering("required", "\$.metainfo: required property 'premiemodell' not found"),
            )

    }
    private fun tagsSomKreverPremiemodell(): List<String> {
        return listOf(
            "fakturering", "prognose", "agresso"
        )
    }

    @Test
    fun `En operasjon skal alltid ha egenskapen handling`() {

        val ingenEgenskaper = """
             {
                "navn": "En test",
                "metainfo": {
                    "mal": "maler/rutinefil.json",
                    "tags": ["eksport"],
                    "støttetAvPaOrkBa01FraVersjon": "0.1.8"
                 },
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
                "metainfo": {
                    "mal": "maler/rutinefil.json",
                    "tags": ["eksport"],
                    "støttetAvPaOrkBa01FraVersjon": "0.1.8"
                 },
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