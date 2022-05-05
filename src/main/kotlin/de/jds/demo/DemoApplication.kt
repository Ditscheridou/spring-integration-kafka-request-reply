package de.jds.demo

import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate

@SpringBootApplication
class DemoApplication : CommandLineRunner {
    private val logger = LoggerFactory.getLogger(DemoApplication::class.java.name)

    @Autowired
    lateinit var kafkaTemplate: ReplyingKafkaTemplate<String?, RequestDTO, ResponseDTO>

    override fun run(vararg args: String?) {
        val record = ProducerRecord<String?, RequestDTO>("kafkaRequests", RequestDTO().apply { id = 5 })
        val response = kafkaTemplate.sendAndReceive(record).get()
        logger.info(response.toString())
    }
}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
