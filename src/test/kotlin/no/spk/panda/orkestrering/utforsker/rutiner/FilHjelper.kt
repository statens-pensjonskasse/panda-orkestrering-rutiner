package no.spk.panda.orkestrering.utforsker.rutiner

import com.networknt.schema.InputFormat
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import java.io.File

data class SkjemaValidering(
    val type: String,
    val melding: String
)

fun validerJsonSkjema(jsonSkjema: String, jsonTekst: String): List<SkjemaValidering> {
    val schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
    val schema: JsonSchema = schemaFactory.getSchema(jsonSkjema)
    val validationErrors = schema.validate(jsonTekst, InputFormat.JSON)

    return validationErrors.map {
        SkjemaValidering(it.type, it.message)
    }
}

fun hentAlleJsonFiler(katalog: File): List<File> =
    katalog
        .walkTopDown()
        .filter { it.isFile }
        .filter { it.extension == "json" }
        .toList()

