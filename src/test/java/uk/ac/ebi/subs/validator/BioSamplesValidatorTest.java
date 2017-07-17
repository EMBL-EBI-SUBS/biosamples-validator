package uk.ac.ebi.subs.validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.validator.biosamples.BiosamplesValidator;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationStatus;

import java.util.Arrays;

import static uk.ac.ebi.subs.validator.TestUtils.generateSample;
import static uk.ac.ebi.subs.validator.TestUtils.generateSampleRelationship;
import static uk.ac.ebi.subs.validator.TestUtils.generateValidationMessageEnvelope;

public class BioSamplesValidatorTest {

    private BiosamplesValidator validator;

    private ValidationMessageEnvelope envelope;
    private Sample sample;

    @Before
    public void setUp() {
        validator = new BiosamplesValidator();
        sample = generateSample("sampleAlias");
    }

    @Test
    public void sampleOkTest() {
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResultsEnvelope = validator.validateSample(envelope);
        Assert.assertTrue(validationResultsEnvelope.getSingleValidationResults().get(0).getValidationStatus().equals(ValidationStatus.Pass));
    }

    @Test
    public void sampleAliasMissingTest() {
        sample.setAlias(null);
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResultEnvelope1 = validator.validateSample(envelope);
        Assert.assertTrue(validationResultEnvelope1.getSingleValidationResults().get(0).getMessage().contains(validator.NAME_MISSING));
        Assert.assertTrue(validationResultEnvelope1.getSingleValidationResults().get(0).getValidationStatus().equals(ValidationStatus.Error));

        sample.setAlias("");
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResultEnvelope2 = validator.validateSample(envelope);
        Assert.assertTrue(validationResultEnvelope2.getSingleValidationResults().get(0).getMessage().contains(validator.NAME_MISSING));
    }

    @Test
    public void sampleReleaseDateMissingTest() {
        sample = generateSample("sampleAlias", "");
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResult = validator.validateSample(envelope);
        Assert.assertTrue(validationResult.getSingleValidationResults().get(0).getMessage().contains("A sample must have a release date."));
    }

    @Test
    public void sampleRelationshipNatureMissingTest() {
        sample.setSampleRelationships(Arrays.asList(generateSampleRelationship("SAM12345", "")));
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResult = validator.validateSample(envelope);
        Assert.assertTrue(validationResult.getSingleValidationResults().get(0).getMessage().contains("A SampleRelationship must have a RelationshipNature."));
    }

    @Test
    public void sampleRelationshipNatureUnknownTest() {
        sample.setSampleRelationships(Arrays.asList(generateSampleRelationship("SAM12345", "created from")));
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResult = validator.validateSample(envelope);
        Assert.assertTrue(validationResult.getSingleValidationResults().get(0).getMessage().contains("unknown, please verify if you wish to proceed."));
    }

    @Test
    public void sampleRelationshipTargetMissingTest() {
        sample.setSampleRelationships(Arrays.asList(generateSampleRelationship(null, "derived from")));
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResult = validator.validateSample(envelope);
        Assert.assertTrue(validationResult.getSingleValidationResults().get(0).getMessage().contains("A SampleRelationship must have a sample accession target."));
    }

    @Test
    public void multipleWarningAndErrorsTest() {
        sample = generateSample("", "");
        sample.setSampleRelationships(Arrays.asList(generateSampleRelationship("SAM12345", "created from")));
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResult = validator.validateSample(envelope);
        Assert.assertTrue(validationResult.getSingleValidationResults().get(0).getValidationStatus().equals(ValidationStatus.Error));
    }
}
