package de.jds.demo.transformer

import org.springframework.integration.transformer.GenericTransformer

/**
 * Transform a string to an all uppercase String
 * @author dittrich
 */
class UppercaseTransformer : GenericTransformer<String, String> {

    override fun transform(source: String): String {
        return source.uppercase()
    }
}
