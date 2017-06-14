package uk.ac.ebi.subs.validator.biosamples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationAuthor;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationStatus;
import uk.ac.ebi.subs.validator.messaging.Exchanges;
import uk.ac.ebi.subs.validator.messaging.Queues;
import uk.ac.ebi.subs.validator.messaging.RoutingKeys;

import java.util.UUID;

@Service
public class ValidatorListener {
    private static Logger logger = LoggerFactory.getLogger(ValidatorListener.class);

    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Autowired
    public ValidatorListener(RabbitMessagingTemplate rabbitMessagingTemplate, MessageConverter messageConverter) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
        this.rabbitMessagingTemplate.setMessageConverter(messageConverter);
    }

    @RabbitListener(queues = Queues.BIOSAMPLES_SAMPLE_VALIDATION)
    public void handleValidationRequest(ValidationMessageEnvelope envelope) {
        logger.debug("Got sample to validate.");

        Sample sample = (Sample) envelope.getEntityToValidate();
        SingleValidationResult result = validate(sample);

        logger.debug("Validation done.");

        result.setValidationResultUUID(envelope.getValidationResultUUID());

        sendResults(result);
    }

    private SingleValidationResult validate(Sample sample) {
        SingleValidationResult result = new SingleValidationResult();
        result.setUuid(UUID.randomUUID().toString());
        result.setEntityUuid(sample.getId());
        result.setMessage("Valid sample.");
        result.setValidationAuthor(ValidationAuthor.Biosamples);
        result.setValidationStatus(ValidationStatus.Pass);
        return result;
    }

    private void sendResults(SingleValidationResult singleValidationResult) {
        if (singleValidationResult.getValidationStatus().equals(ValidationStatus.Error)) {
            rabbitMessagingTemplate.convertAndSend(Exchanges.VALIDATION, RoutingKeys.EVENT_VALIDATION_ERROR, singleValidationResult);
        } else {
            rabbitMessagingTemplate.convertAndSend(Exchanges.VALIDATION, RoutingKeys.EVENT_VALIDATION_SUCCESS, singleValidationResult);

        }
    }

}
