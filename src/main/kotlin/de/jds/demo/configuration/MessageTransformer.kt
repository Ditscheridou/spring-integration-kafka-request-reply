package de.jds.demo.configuration

import org.springframework.integration.transformer.GenericTransformer

class MessageTransformer : GenericTransformer<String, String> {
    override fun transform(source: String): String {
        return source.uppercase()
    }
}
