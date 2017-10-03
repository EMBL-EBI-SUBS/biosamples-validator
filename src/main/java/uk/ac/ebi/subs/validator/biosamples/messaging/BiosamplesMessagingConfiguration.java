package uk.ac.ebi.subs.validator.biosamples.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.subs.messaging.ExchangeConfig;
import uk.ac.ebi.subs.messaging.Queues;

import static uk.ac.ebi.subs.validator.biosamples.messaging.BiosamplesValidatorQueues.BIOSAMPLES_SAMPLE_VALIDATION;
import static uk.ac.ebi.subs.validator.biosamples.messaging.BiosamplesValidatorRoutingKeys.EVENT_BIOSAMPLES_SAMPLE_VALIDATION;

/**
 * RabbitMQ related messaging configuration for the Biosamples queue(s) and binding(s).
 *
 * Created by karoly on 06/07/2017.
 */
@Configuration
@ComponentScan(basePackageClasses = ExchangeConfig.class)
public class BiosamplesMessagingConfiguration {

    /**
     * Instantiate a {@link Queue} for validate samples related to BioSamples.
     *
     * @return an instance of a {@link Queue} for validate samples related to BioSamples.
     */
    @Bean
    Queue biosamplesSampleQueue() {
        return Queues.buildQueueWithDlx(BIOSAMPLES_SAMPLE_VALIDATION);
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
                .with(EVENT_BIOSAMPLES_SAMPLE_VALIDATION);
    }
}
