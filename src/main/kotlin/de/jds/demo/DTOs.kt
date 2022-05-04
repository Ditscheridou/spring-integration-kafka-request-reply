package de.jds.demo

class RequestDTO() {
    var id: Long = 0
    override fun toString(): String {
        return "RequestDTO(id=$id)"
    }
}

class ResponseDTO() {
    var id: Long = 0
    override fun toString(): String {
        return "ResponseDTO(id=$id)"
    }
}
