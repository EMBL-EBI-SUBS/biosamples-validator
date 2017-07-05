package uk.ac.ebi.subs.validator.biosamples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.data.component.SampleRelationship;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationAuthor;
import uk.ac.ebi.subs.validator.data.ValidationStatus;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BiosamplesValidator {
    private static Logger logger = LoggerFactory.getLogger(BiosamplesValidator.class);

    private final List<String> relationshipNatureValues = Arrays.asList("derived from", "child of", "same as", "recurated from");

    private final String NAME_MISSING = "A sample must have an alias.";

    private final String MULTIPLE_DATES_ERROR = "A sample must only have ONE %s date.";
    private final String MISSING_DATE_VALUE = "A sample must have a %s date.";

    private final String SAMPLE_RELATIONSHIP_NULL = "When present, a SampleRelationship must not be null.";
    private final String SAMPLE_RELATIONSHIP_NATURE_MISSING = "A SampleRelationship must have a RelationshipNature.";
    private final String SAMPLE_RELATIONSHIP_NATURE_UNKNOWN = "SampleRelationship [%s] unknown, please verify if you which to proceed.";
    private final String SAMPLE_RELATIONSHIP_TARGET_MISSING = "A SampleRelationship must have a sample accession target.";

    public SingleValidationResult validateSample(Sample sample) {
        SingleValidationResult singleValidationResult = generateSingleValidationResult(sample);

        validateName(sample.getAlias(), singleValidationResult);
        validateDates(sample.getAttributes(), singleValidationResult);
        validateSampleRelationships(sample.getSampleRelationships(), singleValidationResult);

        return singleValidationResult;
    }

    /**
     * A name in the biosamples sample object is the same as the alias in the USI sample object.
     * @param alias
     * @param singleValidationResult
     */
    private void validateName(String alias, SingleValidationResult singleValidationResult) {
        if (alias == null || alias.isEmpty()) {
            singleValidationResult.setValidationStatus(ValidationStatus.Error);
            setErrorMessage(singleValidationResult, NAME_MISSING);
        }
    }

    /**
     * Update and release dates must always be present and never more than once.
     * @param attributes
     * @param singleValidationResult
     */
    private void validateDates(List<Attribute> attributes, SingleValidationResult singleValidationResult) {
        // Release date validation
        List<Attribute> releaseDates = attributes.stream()
                .filter(
                attribute -> attribute.getName().equals("release"))
                .collect(Collectors.toList());

        validateDates(releaseDates, singleValidationResult, "release");

        // Update date validation
        List<Attribute> updateDates = attributes.stream()
                .filter(
                        attribute -> attribute.getName().equals("update"))
                .collect(Collectors.toList());

        validateDates(updateDates, singleValidationResult, "update");
    }

    /**
     * Generic date presence validation.
     * @param dates
     * @param singleValidationResult
     * @param date
     */
    private void validateDates(List<Attribute> dates, SingleValidationResult singleValidationResult, String date) {
        if (dates.size() > 1) {
            setErrorMessage(singleValidationResult, String.format(MULTIPLE_DATES_ERROR, date));
        } else if (dates.get(0) != null) {
            if (dates.get(0).getValue() == null || dates.get(0).getValue().isEmpty()) {
                singleValidationResult.setValidationStatus(ValidationStatus.Error);
                setErrorMessage(singleValidationResult, String.format(MISSING_DATE_VALUE, date));
            }
        } else {
            setErrorMessage(singleValidationResult, String.format(MISSING_DATE_VALUE, date));
        }
    }

    private void validateSampleRelationships(List<SampleRelationship> sampleRelationshipList, SingleValidationResult singleValidationResult) {
        for (SampleRelationship relationship : sampleRelationshipList) {
            validateSampleRelationship(relationship, singleValidationResult);
        }
    }

    /**
     * If present, sample relationships must have a nature and target.
     * @param sampleRelationship
     * @param singleValidationResult
     */
    private void validateSampleRelationship(SampleRelationship sampleRelationship, SingleValidationResult singleValidationResult) {
        if (sampleRelationship != null) {

            // Check for nature
            if (sampleRelationship.getRelationshipNature() == null || sampleRelationship.getRelationshipNature().isEmpty()) {
                singleValidationResult.setValidationStatus(ValidationStatus.Error);
                setErrorMessage(singleValidationResult, SAMPLE_RELATIONSHIP_NATURE_MISSING);
            } else {
                if (!relationshipNatureValues.contains(sampleRelationship.getRelationshipNature())) {
                    if (singleValidationResult.getValidationStatus().equals(ValidationStatus.Pending)) {
                        singleValidationResult.setValidationStatus(ValidationStatus.Warning);
                    }
                    setErrorMessage(singleValidationResult, String.format(SAMPLE_RELATIONSHIP_NATURE_UNKNOWN, sampleRelationship.getRelationshipNature()));
                }
            }

            // Check for target
            if (sampleRelationship.getAccession() == null || sampleRelationship.getAccession().isEmpty()) {
                singleValidationResult.setValidationStatus(ValidationStatus.Error);
                setErrorMessage(singleValidationResult, SAMPLE_RELATIONSHIP_TARGET_MISSING);
            }
        } else {
            singleValidationResult.setValidationStatus(ValidationStatus.Error);
            setErrorMessage(singleValidationResult, SAMPLE_RELATIONSHIP_NULL);
        }
    }

    // -- Helper Methods -- //

    private SingleValidationResult generateSingleValidationResult(Sample sample) {
        SingleValidationResult result = new SingleValidationResult();
        result.setUuid(UUID.randomUUID().toString());
        result.setEntityUuid(sample.getId());
        result.setValidationAuthor(ValidationAuthor.Biosamples);
        result.setValidationStatus(ValidationStatus.Pending);
        return result;
    }

    private void setErrorMessage(SingleValidationResult singleValidationResult, String message) {
        if (singleValidationResult.getValidationStatus().equals(ValidationStatus.Pending)) {
            singleValidationResult.setMessage(message);
        } else {
            String composedMessage = singleValidationResult.getMessage() + " " + message;
            singleValidationResult.setMessage(composedMessage);
        }
    }
}
