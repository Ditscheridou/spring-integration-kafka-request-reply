package de.jds.demo

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.util.concurrent.ListenableFutureCallback
import java.time.Duration

@SpringBootApplication
class DemoApplication : CommandLineRunner {
    @Autowired
    lateinit var kafkaTemplate: ReplyingKafkaTemplate<String?, RequestDTO, ResponseDTO>

    override fun run(vararg args: String?) {
        val record = ProducerRecord<String?, RequestDTO>("kafkaRequests", RequestDTO().apply { id = 5 }).apply {
            headers().add(KafkaHeaders.TOPIC, "kafkaRequests".encodeToByteArray())
                .add(KafkaHeaders.REPLY_TOPIC, "kafkaReplies".encodeToByteArray())
        }
        val response = kafkaTemplate.sendAndReceive(record, Duration.ofMillis(4000))
        response.addCallback(object : ListenableFutureCallback<ConsumerRecord<String?, ResponseDTO>> {
            override fun onSuccess(result: ConsumerRecord<String?, ResponseDTO>?) {
                println("Response ${result?.value()}")
            }

            override fun onFailure(ex: Throwable) {
                println("Response $ex")
            }
        })
    }
}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}
