package uk.ac.ebi.subs.validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.validator.biosamples.BiosamplesValidator;
import uk.ac.ebi.subs.validator.data.SingleValidationResult;
import uk.ac.ebi.subs.validator.data.ValidationStatus;

import java.util.Arrays;

import static uk.ac.ebi.subs.validator.TestUtils.generateSample;
import static uk.ac.ebi.subs.validator.TestUtils.generateSampleRelationship;

public class BioSamplesValidatorTest {

    private BiosamplesValidator validator;

    private Sample sample;

    @Before
    public void setUp() {
        validator = new BiosamplesValidator();
        sample = generateSample("sampleAlias");
    }

    @Test
    public void sampleAliasMissingTest() {
        sample.setAlias(null);
        SingleValidationResult validationResult1 = validator.validateSample(sample);
        Assert.assertTrue(validationResult1.getMessage().contains(validator.NAME_MISSING));

        sample.setAlias("");
        SingleValidationResult validationResult2 = validator.validateSample(sample);
        Assert.assertTrue(validationResult2.getMessage().contains(validator.NAME_MISSING));
    }

    @Test
    public void sampleReleaseDateMissingTest() {
        sample = generateSample("sampleAlias", "");
        SingleValidationResult validationResult = validator.validateSample(sample);
        Assert.assertTrue(validationResult.getMessage().contains("A sample must have a release date."));
    }

    @Test
    public void sampleRelationshipNatureMissingTest() {
        sample.setSampleRelationships(Arrays.asList(generateSampleRelationship("SAM12345", "")));
        SingleValidationResult validationResult = validator.validateSample(sample);
        Assert.assertTrue(validationResult.getMessage().contains("A SampleRelationship must have a RelationshipNature."));
    }

    @Test
    public void sampleRelationshipNatureUnknownTest() {
        sample.setSampleRelationships(Arrays.asList(generateSampleRelationship("SAM12345", "created from")));
        SingleValidationResult validationResult = validator.validateSample(sample);
        Assert.assertTrue(validationResult.getMessage().contains("unknown, please verify if you wish to proceed."));
    }

    @Test
    public void sampleRelationshipTargetMissingTest() {
        sample.setSampleRelationships(Arrays.asList(generateSampleRelationship(null, "derived from")));
        SingleValidationResult validationResult = validator.validateSample(sample);
        System.out.println(validationResult);
        Assert.assertTrue(validationResult.getMessage().contains("A SampleRelationship must have a sample accession target."));
    }

    @Test
    public void multipleWarningAndErrorsTest() {
        sample = generateSample("", "");
        sample.setSampleRelationships(Arrays.asList(generateSampleRelationship("SAM12345", "created from")));
        SingleValidationResult validationResult = validator.validateSample(sample);
        Assert.assertTrue(validationResult.getValidationStatus().equals(ValidationStatus.Error));

    }
}
