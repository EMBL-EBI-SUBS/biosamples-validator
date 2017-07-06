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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BiosamplesValidator {
    private static Logger logger = LoggerFactory.getLogger(BiosamplesValidator.class);

    private final List<String> relationshipNatureValues = Arrays.asList("derived from", "child of", "same as", "recurated from"); // FIXME - fill list from properties

    public final String NAME_MISSING = "A sample must have an alias.";

    private final String MULTIPLE_DATES_ERROR = "A sample must only have ONE release date.";
    private final String MISSING_DATE_VALUE = "A sample must have a release date.";
    private final String DATE_WRONG_FORMAT = "The release date must comply with ISO 8601.";

    private final String SAMPLE_RELATIONSHIP_NULL = "When present, a SampleRelationship must not be null.";
    private final String SAMPLE_RELATIONSHIP_NATURE_MISSING = "A SampleRelationship must have a RelationshipNature.";
    private final String SAMPLE_RELATIONSHIP_NATURE_UNKNOWN = "SampleRelationship nature: [%s] unknown, please verify if you wish to proceed.";
    private final String SAMPLE_RELATIONSHIP_TARGET_MISSING = "A SampleRelationship must have a sample accession target.";

    public SingleValidationResult validateSample(Sample sample) {
        SingleValidationResult singleValidationResult = generateSingleValidationResult(sample);

        validateName(sample.getAlias(), singleValidationResult);
        validateReleaseDate(sample.getAttributes(), singleValidationResult);
        validateSampleRelationships(sample.getSampleRelationships(), singleValidationResult);

        if (singleValidationResult.getValidationStatus().equals(ValidationStatus.Pending)) {
            singleValidationResult.setValidationStatus(ValidationStatus.Complete);
        }

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
     * Release date must always be present and never more than once.
     * @param attributes
     * @param singleValidationResult
     */
    private void validateReleaseDate(List<Attribute> attributes, SingleValidationResult singleValidationResult) {
        List<Attribute> releaseDates = attributes.stream()
                .filter(
                attribute -> attribute.getName().equals("release"))
                .collect(Collectors.toList());

        if (releaseDates.size() > 1) {
            setErrorMessage(singleValidationResult, MULTIPLE_DATES_ERROR);
            return;
        }

        if (releaseDates.size() == 1 && releaseDates.get(0) != null) {
            if (releaseDates.get(0).getValue() == null || releaseDates.get(0).getValue().isEmpty()) {
                singleValidationResult.setValidationStatus(ValidationStatus.Error);
                setErrorMessage(singleValidationResult, MISSING_DATE_VALUE);
            } else {
                if (!validateDateFormat(releaseDates.get(0).getValue())) {
                    singleValidationResult.setValidationStatus(ValidationStatus.Error);
                    setErrorMessage(singleValidationResult, DATE_WRONG_FORMAT);
                }
            }
        }

    }

    private boolean validateDateFormat(String releaseDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime.parse(releaseDate, formatter);
        } catch (Exception e) {
            logger.debug("Invalid date format: " + releaseDate);
            return false;
        }
        return true;
    }

    private void validateSampleRelationships(List<SampleRelationship> sampleRelationshipList, SingleValidationResult singleValidationResult) {
        if(sampleRelationshipList != null && !sampleRelationshipList.isEmpty()) {
            for (SampleRelationship relationship : sampleRelationshipList) {
                validateSampleRelationship(relationship, singleValidationResult);
            }
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
