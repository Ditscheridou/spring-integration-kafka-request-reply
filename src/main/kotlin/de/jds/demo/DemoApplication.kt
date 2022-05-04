package de.jds.demo

import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.KafkaHeaders

@SpringBootApplication
class DemoApplication() : CommandLineRunner {
    @Autowired
    lateinit var kafkaTemplate: ReplyingKafkaTemplate<String, String, String>

    override fun run(vararg args: String?) {
        val response = kafkaTemplate.sendAndReceive(
            ProducerRecord<String?, String?>("kafkaRequests", "test").apply {
                headers().add(KafkaHeaders.TOPIC, "kafkaRequests".encodeToByteArray())
                    .add(KafkaHeaders.CORRELATION_ID, "blabla".encodeToByteArray())
                    .add(KafkaHeaders.REPLY_TOPIC, "kafkaReplies".encodeToByteArray())
            }
        ).get()
        println("Response $response")
    }
}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
