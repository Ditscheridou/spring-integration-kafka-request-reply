package de.jds.demo.transformer

import de.jds.demo.RequestDTO
import de.jds.demo.ResponseDTO
import org.springframework.integration.transformer.GenericTransformer

class RequestResponseDTOsTransformer : GenericTransformer<RequestDTO, ResponseDTO> {
    override fun transform(source: RequestDTO): ResponseDTO {
        return ResponseDTO().apply {
            id = source.id
        }
    }
}
