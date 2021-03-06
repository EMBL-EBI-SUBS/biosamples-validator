package uk.ac.ebi.subs.validator.biosamples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.data.component.SampleRelationship;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;
import uk.ac.ebi.subs.validator.data.structures.ValidationAuthor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A Spring @Service component to validate sample.
 * It validates the sample's alias, release date and its relationships to other samples
 * that already exists in BioSamples database.
 */
@Service
public class BiosamplesValidator {
    private static Logger logger = LoggerFactory.getLogger(BiosamplesValidator.class);

    private final List<String> relationshipNatureValues = Arrays.asList("derived from", "child of", "same as", "recurated from"); // FIXME - fill list from properties

    public final String NAME_MISSING = "A sample must have an alias.";

    private final String MISSING_DATE_VALUE = "A sample must have a release date.";

    private final String SAMPLE_RELATIONSHIP_NULL = "When present, a SampleRelationship must not be null.";
    private final String SAMPLE_RELATIONSHIP_NATURE_MISSING = "A SampleRelationship must have a RelationshipNature.";
    private final String SAMPLE_RELATIONSHIP_NATURE_UNKNOWN = "SampleRelationship nature: [%s] unknown, please verify if you wish to proceed.";
    private final String SAMPLE_RELATIONSHIP_TARGET_MISSING = "A SampleRelationship must have a target sample accession or alias.";

    public SingleValidationResultsEnvelope validateSample(ValidationMessageEnvelope envelope) {
        Sample sample = (Sample) envelope.getEntityToValidate();

        List<SingleValidationResult> singleValidationResults = new ArrayList<>();

        singleValidationResults.add(validateSampleName(sample));
        singleValidationResults.add(validateReleaseDate(sample));
        singleValidationResults.addAll(validateSampleRelationships(sample));

        // List of errors and/or warnings
        List errorsList = singleValidationResults.stream().filter(singleValidationResult -> !singleValidationResult.getValidationStatus().equals(SingleValidationResultStatus.Pass)).collect(Collectors.toList());
        if (!errorsList.isEmpty()) {
            return generateSingleValidationResultsEnvelope(errorsList, envelope);
        } else {
            return generateSingleValidationResultsEnvelope(Arrays.asList(generateDefaultSingleValidationResult(sample.getId())), envelope);
        }
    }

    /**
     * A name in the biosamples sample object is the same as the alias in the USI sample object.
     * @param sample
     *
     */
    private SingleValidationResult validateSampleName(Sample sample) {
        String alias = sample.getAlias();
        SingleValidationResult singleValidationResult = generateDefaultSingleValidationResult(sample.getId());

        if (alias == null || alias.isEmpty()) {
            singleValidationResult.setValidationStatus(SingleValidationResultStatus.Error);
            singleValidationResult.setMessage(NAME_MISSING);
        }
        return singleValidationResult;
    }

    /**
     * Release date must always be present and never more than once.
     * @param sample
     */
    private SingleValidationResult validateReleaseDate(Sample sample) {
        SingleValidationResult singleValidationResult = generateDefaultSingleValidationResult(sample.getId());

        LocalDate releaseDate = sample.getReleaseDate();

        if (releaseDate == null) {
            singleValidationResult.setValidationStatus(SingleValidationResultStatus.Error);
            singleValidationResult.setMessage(MISSING_DATE_VALUE);
            return singleValidationResult;
        }

        return singleValidationResult;
    }

    /**
     * Iterate over a List of SampleRelationship
     * @param sample to get the {@link SampleRelationship} property from
     */
    private List<SingleValidationResult> validateSampleRelationships(Sample sample) {
        List<SampleRelationship> sampleRelationshipList = sample.getSampleRelationships();
        List<SingleValidationResult> singleValidationResults = new ArrayList<>();

        for (SampleRelationship relationship : sampleRelationshipList) {
            SingleValidationResult singleValidationResult = generateDefaultSingleValidationResult(sample.getId());
            validateSampleRelationship(relationship, singleValidationResult);
            singleValidationResults.add(singleValidationResult);
        }

        return singleValidationResults;
    }

    /**
     * If present, sample relationships must have a nature and target.
     * @param sampleRelationship
     */
    private void validateSampleRelationship(SampleRelationship sampleRelationship, SingleValidationResult singleValidationResult) {
        if (sampleRelationship != null) {

            // Check for nature
            if (sampleRelationship.getRelationshipNature() == null || sampleRelationship.getRelationshipNature().isEmpty()) {
                singleValidationResult.setValidationStatus(SingleValidationResultStatus.Error);
                singleValidationResult.setMessage(SAMPLE_RELATIONSHIP_NATURE_MISSING);
                return;
            }

            // Check for target
            if (noAccession(sampleRelationship) && noAlias(sampleRelationship)) {
                singleValidationResult.setValidationStatus(SingleValidationResultStatus.Error);
                singleValidationResult.setMessage(SAMPLE_RELATIONSHIP_TARGET_MISSING);
                return;
            }

            // Check known nature
            if (!relationshipNatureValues.contains(sampleRelationship.getRelationshipNature())) {
                singleValidationResult.setValidationStatus(SingleValidationResultStatus.Warning);
                singleValidationResult.setMessage(String.format(SAMPLE_RELATIONSHIP_NATURE_UNKNOWN, sampleRelationship.getRelationshipNature()));
            }

        } else {
            singleValidationResult.setValidationStatus(SingleValidationResultStatus.Error);
            singleValidationResult.setMessage(SAMPLE_RELATIONSHIP_NULL);
        }
    }

    private boolean noAccession(SampleRelationship sampleRelationship) {
        return sampleRelationship.getAccession() == null || sampleRelationship.getAccession().trim().isEmpty();
    }

    private boolean noAlias(SampleRelationship sampleRelationship){
        return sampleRelationship.getAlias() == null || sampleRelationship.getAlias().trim().isEmpty();
    }

    private SingleValidationResult generateDefaultSingleValidationResult(String sampleId) {
        SingleValidationResult result = new SingleValidationResult(ValidationAuthor.Biosamples, sampleId);
        result.setValidationStatus(SingleValidationResultStatus.Pass);
        return result;
    }

    private SingleValidationResultsEnvelope generateSingleValidationResultsEnvelope(List<SingleValidationResult> singleValidationResults, ValidationMessageEnvelope envelope) {
        return new SingleValidationResultsEnvelope(singleValidationResults, envelope.getValidationResultVersion(), envelope.getValidationResultUUID(), ValidationAuthor.Biosamples);
    }
}
