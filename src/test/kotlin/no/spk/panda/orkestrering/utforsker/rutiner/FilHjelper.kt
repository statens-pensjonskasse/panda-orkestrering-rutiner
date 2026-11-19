package no.spk.panda.orkestrering.utforsker.rutiner

import com.networknt.schema.InputFormat
import com.networknt.schema.Schema
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.SchemaRegistryConfig
import com.networknt.schema.SpecificationVersion
import com.networknt.schema.path.PathType
import java.io.File

data class SkjemaValidering(
    val type: String,
    val sti: String,
    val felt: String? = null,
    // Bare satt når skjemaet har en egen melding under "message"
    val melding: String? = null,
) {
    fun beskrivelse(): String {
        val tekst = melding ?: when (type) {
            "required" -> "mangler påkrevd felt '$felt'"
            else -> "bryter regelen '$type'" + (felt?.let { " for felt '$it'" } ?: "")
        }
        return "$sti: $tekst"
    }
}

fun validerJsonSkjema(jsonSkjema: String, jsonTekst: String): List<SkjemaValidering> {
    val config = SchemaRegistryConfig.builder().pathType(PathType.JSON_PATH).errorMessageKeyword("message").build()
    val schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12) { it.schemaRegistryConfig(config) }
    val schema: Schema = schemaRegistry.getSchema(jsonSkjema, InputFormat.JSON)
    val validationErrors = schema.validate(jsonTekst, InputFormat.JSON)

    return validationErrors.map {
        SkjemaValidering(
            type = it.keyword,
            sti = it.instanceLocation.toString(),
            felt = it.property,
            melding = if (it.isCustomMessage) it.message else null,
        )
    }
}

fun hentAlleJsonFiler(katalog: File): List<File> =
    katalog
        .walkTopDown()
        .filter { it.isFile }
        .filter { it.extension == "json" }
        .toList()

