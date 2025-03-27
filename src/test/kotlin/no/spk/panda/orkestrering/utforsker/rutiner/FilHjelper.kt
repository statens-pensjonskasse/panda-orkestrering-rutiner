package no.spk.panda.orkestrering.utforsker.rutiner

import com.networknt.schema.InputFormat
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import java.io.File
import java.util.*

data class SkjemaValidering(
    val type: String,
    val melding: String
)

fun validerJsonSkjema(jsonSkjema: String, jsonTekst: String): List<SkjemaValidering> {
    // JsonSchema validatoren forholder seg til Locale, noe som gjorde testene flaky
    // Setter derfor Locale til engelsk for å unngå at testene feiler på grunn av språk
    val originalLocale = Locale.getDefault()
    try {
        Locale.setDefault(Locale.ENGLISH)

        // Perform validation
        val schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
        val schema: JsonSchema = schemaFactory.getSchema(jsonSkjema)
        val validationErrors = schema.validate(jsonTekst, InputFormat.JSON)

        return validationErrors.map {
            SkjemaValidering(it.type, it.message)
        }
    } finally {
        // Setter locale tilbake til den orginale verdien
        Locale.setDefault(originalLocale)
    }
}

fun hentAlleJsonFiler(katalog: File): List<File> =
    katalog
        .walkTopDown()
        .filter { it.isFile }
        .filter { it.extension == "json" }
        .toList()

