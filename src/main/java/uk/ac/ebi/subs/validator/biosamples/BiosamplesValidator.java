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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BiosamplesValidator {
    private static Logger logger = LoggerFactory.getLogger(BiosamplesValidator.class);

    private static final String SAMPLE_RELATIONSHIP_NULL = "A SampleRelationship must not be null.";
    private static final String SAMPLE_RELATIONSHIP_NATURE_MISSING = "A SampleRelationship must have a RelationshipNature.";

    private static final String MULTIPLE_RELEASE_DATES_ERROR = "A sample must only have ONE release date.";
    private static final String MISSING_DATE_VALUE = "A sample must have a %s date.";

    // validate release date

    // validate update date

    // validate sample relationship makes sense to biosamples

    public SingleValidationResult validateSample(Sample sample) {
        SingleValidationResult singleValidationResult = generateSingleValidationResult(sample);

        validateDates(sample.getAttributes(), singleValidationResult);
        validateSampleRelationships(sample.getSampleRelationships(), singleValidationResult);

        return singleValidationResult;
    }

    private void validateDates(List<Attribute> attributes, SingleValidationResult singleValidationResult) {
        List<Attribute> attributeDates = attributes.stream()
                .filter(
                attribute -> attribute.getName().equals("release"))
                .collect(Collectors.toList());

        if (attributeDates.size() > 1) {
            setErrorMessage(singleValidationResult, MULTIPLE_RELEASE_DATES_ERROR);
        } else {
            if (attributeDates.get(0).getValue() == null || attributeDates.get(0).getValue().isEmpty()) {
                setErrorMessage(singleValidationResult, String.format(MISSING_DATE_VALUE, "release"));
            }
        }
    }

    private void validateSampleRelationships(List<SampleRelationship> sampleRelationshipList, SingleValidationResult singleValidationResult) {
        for (SampleRelationship relationship : sampleRelationshipList) {
            validateSampleRelationship(relationship, singleValidationResult);
        }
    }

    private void validateSampleRelationship(SampleRelationship sampleRelationship, SingleValidationResult singleValidationResult) {
        if(sampleRelationship.getRelationshipNature() != null) {
            if (sampleRelationship.getRelationshipNature() == null || sampleRelationship.getRelationshipNature().isEmpty()) {
                singleValidationResult.setValidationStatus(ValidationStatus.Error);
                setErrorMessage(singleValidationResult, SAMPLE_RELATIONSHIP_NATURE_MISSING);
            }
        } else {
            singleValidationResult.setValidationStatus(ValidationStatus.Error);
            setErrorMessage(singleValidationResult, SAMPLE_RELATIONSHIP_NULL);
        }
    }

    private SingleValidationResult generateSingleValidationResult(Sample sample) {
        SingleValidationResult result = new SingleValidationResult();
        result.setUuid(UUID.randomUUID().toString());
        result.setEntityUuid(sample.getId());
        result.setValidationAuthor(ValidationAuthor.Biosamples);
        result.setValidationStatus(ValidationStatus.Pass);
        return result;
    }

    private void setErrorMessage(SingleValidationResult singleValidationResult, String message) {
        // TODO
    }
}
