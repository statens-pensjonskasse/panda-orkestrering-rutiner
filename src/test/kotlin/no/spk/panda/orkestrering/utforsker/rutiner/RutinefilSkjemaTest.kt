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
                SkjemaValidering("required", "$", "navn", "Rutinefilen må inneholde feltet navn."),
                SkjemaValidering("required", "$", "metainfo", "Rutinefilen må inneholde feltet metainfo."),
                SkjemaValidering("required", "$", "variabler", "Rutinefilen må inneholde feltet variabler."),
                SkjemaValidering("required", "$", "operasjoner", "Rutinefilen må inneholde feltet operasjoner."),
            )

    }

    @ParameterizedTest
    @MethodSource("tommeNavn")
    fun `Rutinefilen må ha et navn`(navn: String) {

        val utenNavn = """
             {
                "navn": "$navn",
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
            validerJsonSkjema(rutinefilSkjema, utenNavn)
        )
            .hasSize(1)
            .containsExactlyInAnyOrder(
                SkjemaValidering("pattern", "\$.navn", melding = "Rutinefilen må ha et navn."),
            )

    }
    private fun tommeNavn(): List<String> {
        return listOf("", "   ")
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
                SkjemaValidering("minItems", "\$.operasjoner"),
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
                SkjemaValidering("minItems", "\$.metainfo.tags"),
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
                SkjemaValidering("required", "\$.metainfo", "premiemodell", "Maler tagget med fakturering, prognose, agresso eller kontoutskrift må ha premiemodell i metainfo."),
            )

    }
    private fun tagsSomKreverPremiemodell(): List<String> {
        return listOf(
            "fakturering", "prognose", "agresso"
        )
    }

    @Test
    fun `Rutinefil tagget med metadataBestilling skal kreve variabelen kjøringsId`() {

        val utenKjøringsId = """
             {
                "navn": "En test",
                "metainfo": {
                    "mal": "maler/rutinefil.json",
                    "tags": ["gkrs", "metadataBestilling"],
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
            validerJsonSkjema(rutinefilSkjema, utenKjøringsId)
        )
            .hasSize(1)
            .containsExactlyInAnyOrder(
                SkjemaValidering("required", "\$.variabler", "kjøringsId", "Maler tagget med metadataBestilling må ha variabelen kjøringsId, fordi flytkontroll bruker den til å finne kjøringene i MongoDB."),
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
                SkjemaValidering("required", "\$.operasjoner[0]", "handling"),
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