package uk.ac.ebi.subs.validator.biosamples.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.subs.validator.messaging.Queues;
import uk.ac.ebi.subs.validator.messaging.RoutingKeys;
import uk.ac.ebi.subs.validator.messaging.ValidationExchangeConfig;

/**
 * RabbitMQ related messaging configuration for the Biosamples queue(s) and binding(s).
 *
 * Created by karoly on 06/07/2017.
 */
@Configuration
@ComponentScan(basePackageClasses = ValidationExchangeConfig.class)
public class BiosamplesMessagingConfiguration {

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Instantiate a {@link Queue} for validate samples related to BioSamples.
     *
     * @return an instance of a {@link Queue} for validate samples related to BioSamples.
     */
    @Bean
    Queue biosamplesSampleQueue() {
        return new Queue(Queues.BIOSAMPLES_SAMPLE_VALIDATION, true);
    }

    /**
     * Create a {@link Binding} between the validation exchange and BioSamples sample validation queue
     * using the routing key of created samples related to BioSamples.
     *
     * @param biosamplesSampleQueue {@link Queue} for validating BioSamples related samples
     * @param submissionExchange {@link TopicExchange} for validation
     * @return a {@link Binding} between the validation exchange and BioSamples sample validation queue
     * using the routing key of created samples related to BioSamples.
     */
    @Bean
    Binding validationForCreatedBiosamplesSampleBinding(Queue biosamplesSampleQueue, TopicExchange submissionExchange) {
        return BindingBuilder.bind(biosamplesSampleQueue).to(submissionExchange)
                .with(RoutingKeys.EVENT_BIOSAMPLES_SAMPLE_VALIDATION);
    }
}
