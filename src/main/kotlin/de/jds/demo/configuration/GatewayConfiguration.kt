package de.jds.demo.configuration

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.kafka.dsl.Kafka
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate

@Configuration
@EnableKafka
@EnableIntegration
class GatewayConfiguration {

    private val logger = LoggerFactory.getLogger(GatewayConfiguration::class.java.simpleName)

    // Kafka configuration
    @Bean
    fun replyingTemplate(
        pf: ProducerFactory<String, String>,
        factory: ConcurrentKafkaListenerContainerFactory<String, String>
    ): ReplyingKafkaTemplate<String, String, String> {

        val replyContainer =
            factory.createContainer("kafkaReplies")
        replyContainer.containerProperties.setGroupId("kafkaReplies")
        replyContainer.containerProperties.ackMode = ContainerProperties.AckMode.BATCH
        return ReplyingKafkaTemplate(pf, replyContainer)
    }

    @Bean
    fun replyTemplate(
        pf: ProducerFactory<String, String>,
        factory: ConcurrentKafkaListenerContainerFactory<String, String>
    ): KafkaTemplate<String, String> {

        val kafkaTemplate = KafkaTemplate(pf)
        factory.setReplyTemplate(kafkaTemplate)
        return kafkaTemplate
    }

    @Bean
    fun container(
        consumerFactory: ConsumerFactory<String, String>,
        kafkaProperties: KafkaProperties
    ): ConcurrentMessageListenerContainer<String, String> {
        return ConcurrentMessageListenerContainer(consumerFactory, ContainerProperties("kafkaRequests"))
    }

    // Spring integration part for kafka
    @Bean
    fun serverGateway(
        container: ConcurrentMessageListenerContainer<String, String?>,
        replyTemplate: KafkaTemplate<String?, String?>?
    ): IntegrationFlow? {
        return IntegrationFlows
            .from(
                Kafka.inboundGateway(container, replyTemplate)
                    .replyTimeout(30000)
            )
            // the transformer is put between the request and the reply channel to do stuff with it
            .transform(MessageTransformer())
            .get()
    }
}
