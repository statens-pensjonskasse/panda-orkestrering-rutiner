package no.spk.panda.orkestrering.utforsker.rutiner

import com.networknt.schema.InputFormat
import com.networknt.schema.Schema
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.SchemaRegistryConfig
import com.networknt.schema.SpecificationVersion
import com.networknt.schema.path.PathType
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
        val config = SchemaRegistryConfig.builder().pathType(PathType.JSON_PATH).build()
        val schemaRegistry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12) { it.schemaRegistryConfig(config) }
        val schema: Schema = schemaRegistry.getSchema(jsonSkjema, InputFormat.JSON)
        val validationErrors = schema.validate(jsonTekst, InputFormat.JSON)

        return validationErrors.map {
            SkjemaValidering(it.keyword, "${it.instanceLocation}: ${it.message}")
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

