package uk.ac.ebi.subs.validator.biosamples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.messaging.Exchanges;
import uk.ac.ebi.subs.validator.data.SampleValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;

import java.util.List;
import java.util.stream.Collectors;

import static uk.ac.ebi.subs.validator.biosamples.messaging.BiosamplesValidatorQueues.BIOSAMPLES_SAMPLE_VALIDATION;
import static uk.ac.ebi.subs.validator.biosamples.messaging.BiosamplesValidatorRoutingKeys.EVENT_VALIDATION_ERROR;
import static uk.ac.ebi.subs.validator.biosamples.messaging.BiosamplesValidatorRoutingKeys.EVENT_VALIDATION_SUCCESS;

/**
 * This is a listener to handle the validation of a sample.
 * It executes a validator against the BioSamples' standard.
 * It sends a success or error message after the validation has been executed.
 */
@Service
public class ValidatorListener {
    private static Logger logger = LoggerFactory.getLogger(ValidatorListener.class);

    @Autowired
    private BiosamplesValidator validator;

    private RabbitMessagingTemplate rabbitMessagingTemplate;

    @Autowired
    public ValidatorListener(RabbitMessagingTemplate rabbitMessagingTemplate) {
        this.rabbitMessagingTemplate = rabbitMessagingTemplate;
    }

    @RabbitListener(queues = BIOSAMPLES_SAMPLE_VALIDATION)
    public void handleValidationRequest(SampleValidationMessageEnvelope envelope) {
        logger.info("Received validation request on sample with id {}", envelope.getEntityToValidate().getId());

        SingleValidationResultsEnvelope singleValidationResultsEnvelope = validator.validateSample(envelope);

        sendResults(singleValidationResultsEnvelope);
    }

    private void sendResults(SingleValidationResultsEnvelope envelope) {
        List<SingleValidationResult> errorResults = envelope.getSingleValidationResults().stream().filter(svr -> svr.getValidationStatus().equals(SingleValidationResultStatus.Error)).collect(Collectors.toList());
        if (errorResults.size() > 0) {
            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, EVENT_VALIDATION_ERROR, envelope);
        } else {
            rabbitMessagingTemplate.convertAndSend(Exchanges.SUBMISSIONS, EVENT_VALIDATION_SUCCESS, envelope);
        }
    }
}
