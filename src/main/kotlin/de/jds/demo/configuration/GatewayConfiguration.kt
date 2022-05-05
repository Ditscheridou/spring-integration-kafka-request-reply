package de.jds.demo.configuration

import de.jds.demo.RequestDTO
import de.jds.demo.ResponseDTO
import de.jds.demo.transformer.RequestResponseDTOsTransformer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.kafka.dsl.Kafka
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.*
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer

@Configuration
@EnableKafka
@EnableIntegration
class GatewayConfiguration {
    // this can also be achieved with the {org.springframework.integration.annotation.MessagingGateway} annotation
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
            .transform(RequestResponseDTOsTransformer())
            .get()
    }
}
