package de.jds.demo.configuration

import de.jds.demo.RequestDTO
import de.jds.demo.ResponseDTO
import de.jds.demo.transformer.RequestResponseDTOsTransformer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.kafka.dsl.Kafka
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate
import org.springframework.kafka.support.converter.JsonMessageConverter
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafka
@EnableIntegration
class GatewayConfiguration {

    private val logger = LoggerFactory.getLogger(GatewayConfiguration::class.java.simpleName)

    @Bean
    fun kafkaConsumerFactory(
        customizers: ObjectProvider<DefaultKafkaConsumerFactoryCustomizer>,
        properties: KafkaProperties
    ): ConsumerFactory<String?, ResponseDTO> {
        properties.consumer.apply {
            groupId = "test"
        }
        val factory = DefaultKafkaConsumerFactory<String?, ResponseDTO>(
            properties.buildConsumerProperties().apply {
                put("spring.json.trusted.packages", "*")
            }
        )
        factory.apply {
            setKeyDeserializer(StringDeserializer())
            setValueDeserializer(JsonDeserializer())
        }
        return factory
    }

    @Bean
    fun kafkaProducerFactory(
        customizers: ObjectProvider<DefaultKafkaProducerFactoryCustomizer>,
        properties: KafkaProperties
    ): ProducerFactory<String?, RequestDTO> {
        val factory: DefaultKafkaProducerFactory<String?, RequestDTO> =
            DefaultKafkaProducerFactory<String?, RequestDTO>(
                properties.buildProducerProperties().apply {
                    put("spring.json.trusted.packages", "*")
                }
            )
        val transactionIdPrefix: String? = properties.producer.transactionIdPrefix
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix)
        }
        factory.apply {
            keySerializer = StringSerializer()
            setValueSerializer(JsonSerializer())
        }
        return factory
    }

    // Kafka configuration
    @Bean
    fun replyingTemplate(
        pf: ProducerFactory<String?, RequestDTO>,
        factory: ConcurrentKafkaListenerContainerFactory<String?, ResponseDTO>
    ): ReplyingKafkaTemplate<String?, RequestDTO, ResponseDTO> {

        val replyContainer =
            factory.createContainer("kafkaReplies")
        replyContainer.containerProperties.setGroupId("kafkaReplies")
        replyContainer.containerProperties.ackMode = ContainerProperties.AckMode.BATCH
        return ReplyingKafkaTemplate(pf, replyContainer)
    }

    @Bean
    fun replyTemplate(
        pf: ProducerFactory<String?, RequestDTO>,
        factory: ConcurrentKafkaListenerContainerFactory<String?, ResponseDTO>
    ): KafkaTemplate<String?, RequestDTO> {
        val kafkaTemplate = KafkaTemplate(pf)
        kafkaTemplate.messageConverter = StringJsonMessageConverter()
        factory.setReplyTemplate(kafkaTemplate)
        return kafkaTemplate
    }

    @Bean
    fun container(
        consumerFactory: ConsumerFactory<String?, ResponseDTO>,
        kafkaProperties: KafkaProperties
    ): ConcurrentMessageListenerContainer<String?, ResponseDTO> {
        return ConcurrentMessageListenerContainer(consumerFactory, ContainerProperties("kafkaRequests"))
    }

    // Spring integration part for kafka
    @Bean
    fun serverGateway(
        container: ConcurrentMessageListenerContainer<String?, ResponseDTO>,
        replyTemplate: KafkaTemplate<String?, RequestDTO>
    ): IntegrationFlow? {
        return IntegrationFlows
            .from(
                Kafka.inboundGateway(container, replyTemplate)
                    .replyTimeout(30000)
            )
            // the transformer is put between the request and the reply channel to do stuff with it
//            .transform(JsonToObjectTransformer(RequestDTO::class.java))
            .transform(RequestResponseDTOsTransformer())
            .get()
    }
}
