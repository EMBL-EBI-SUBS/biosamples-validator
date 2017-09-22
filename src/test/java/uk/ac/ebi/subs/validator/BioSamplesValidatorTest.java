package uk.ac.ebi.subs.validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.validator.biosamples.BiosamplesValidator;
import uk.ac.ebi.subs.validator.data.SingleValidationResultsEnvelope;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;
import uk.ac.ebi.subs.validator.data.structures.SingleValidationResultStatus;

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
    public void samplePassTest() {
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResultsEnvelope = validator.validateSample(envelope);

        Assert.assertEquals(1, validationResultsEnvelope.getSingleValidationResults().size());
        Assert.assertTrue(validationResultsEnvelope.getSingleValidationResults().get(0).getValidationStatus().equals(SingleValidationResultStatus.Pass));
        Assert.assertEquals(null, validationResultsEnvelope.getSingleValidationResults().get(0).getMessage());
    }

    @Test
    public void nullAliasTest() {
        sample.setAlias(null);
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResultsEnvelope = validator.validateSample(envelope);

        Assert.assertEquals(1, validationResultsEnvelope.getSingleValidationResults().size());
        Assert.assertTrue(validationResultsEnvelope.getSingleValidationResults().get(0).getValidationStatus().equals(SingleValidationResultStatus.Error));
        Assert.assertTrue(validationResultsEnvelope.getSingleValidationResults().get(0).getMessage().startsWith(validator.NAME_MISSING));
    }

    @Test
    public void emptyAliasTest() {
        sample.setAlias("");
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResultsEnvelope = validator.validateSample(envelope);

        Assert.assertEquals(1, validationResultsEnvelope.getSingleValidationResults().size());
        Assert.assertTrue(validationResultsEnvelope.getSingleValidationResults().get(0).getValidationStatus().equals(SingleValidationResultStatus.Error));
        Assert.assertTrue(validationResultsEnvelope.getSingleValidationResults().get(0).getMessage().startsWith(validator.NAME_MISSING));
    }

    @Test
    public void missingReleaseDateTest() {
        sample = generateSample("sampleAlias", "");
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResultsEnvelope = validator.validateSample(envelope);

        Assert.assertEquals(1, validationResultsEnvelope.getSingleValidationResults().size());
        Assert.assertTrue(validationResultsEnvelope.getSingleValidationResults().get(0).getValidationStatus().equals(SingleValidationResultStatus.Error));
        Assert.assertTrue(validationResultsEnvelope.getSingleValidationResults().get(0).getMessage().startsWith("A sample must have a release date."));
    }

    @Test
    public void missingRelationshipNatureTest() {
        sample.setSampleRelationships(Arrays.asList(generateSampleRelationship("SAM12345", "")));
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResultsEnvelope = validator.validateSample(envelope);

        Assert.assertEquals(1, validationResultsEnvelope.getSingleValidationResults().size());
        Assert.assertTrue(validationResultsEnvelope.getSingleValidationResults().get(0).getValidationStatus().equals(SingleValidationResultStatus.Error));
        Assert.assertTrue(validationResultsEnvelope.getSingleValidationResults().get(0).getMessage().startsWith("A SampleRelationship must have a RelationshipNature."));
    }

    @Test
    public void unknownRelationshipNatureTest() {
        sample.setSampleRelationships(Arrays.asList(generateSampleRelationship("SAM12345", "created from")));
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResultsEnvelope = validator.validateSample(envelope);

        Assert.assertEquals(1, validationResultsEnvelope.getSingleValidationResults().size());
        Assert.assertTrue(validationResultsEnvelope.getSingleValidationResults().get(0).getValidationStatus().equals(SingleValidationResultStatus.Warning));
        Assert.assertTrue(validationResultsEnvelope.getSingleValidationResults().get(0).getMessage().contains("unknown, please verify if you wish to proceed."));
    }

    @Test
    public void missingRelationshipTargeTest() {
        sample.setSampleRelationships(Arrays.asList(generateSampleRelationship(null, "derived from")));
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResultsEnvelope = validator.validateSample(envelope);

        Assert.assertEquals(1, validationResultsEnvelope.getSingleValidationResults().size());
        Assert.assertTrue(validationResultsEnvelope.getSingleValidationResults().get(0).getValidationStatus().equals(SingleValidationResultStatus.Error));
        Assert.assertTrue(validationResultsEnvelope.getSingleValidationResults().get(0).getMessage().startsWith("A SampleRelationship must have a sample accession target."));
    }

    @Test
    public void multipleWarningAndErrorsTest() {
        sample = generateSample("", "");
        sample.setSampleRelationships(Arrays.asList(generateSampleRelationship("SAM12345", "created from")));
        envelope = generateValidationMessageEnvelope(sample);
        SingleValidationResultsEnvelope validationResultsEnvelope = validator.validateSample(envelope);

        Assert.assertEquals(3, validationResultsEnvelope.getSingleValidationResults().size());
        Assert.assertTrue(!validationResultsEnvelope.getSingleValidationResults().get(0).getValidationStatus().equals(SingleValidationResultStatus.Pass));
        Assert.assertTrue(!validationResultsEnvelope.getSingleValidationResults().get(1).getValidationStatus().equals(SingleValidationResultStatus.Pass));
        Assert.assertTrue(!validationResultsEnvelope.getSingleValidationResults().get(2).getValidationStatus().equals(SingleValidationResultStatus.Pass));
    }
}