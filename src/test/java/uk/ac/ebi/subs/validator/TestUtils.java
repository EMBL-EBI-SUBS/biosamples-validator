package uk.ac.ebi.subs.validator;

import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.data.component.SampleRelationship;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class TestUtils {

    public static Sample generateSample(String alias) {
        return generateSample(
                alias,
                LocalDateTime.now().withDayOfMonth(1).withMonth(4).withYear(2018).format(DateTimeFormatter.ISO_DATE_TIME));
    }

    public static Sample generateSample(String alias, String release, String update, List<SampleRelationship> relationships) {
        Sample sample = new Sample();
        sample.setId(UUID.randomUUID().toString());
        sample.setAlias(alias);
        Attribute att = new Attribute();
        att.setName("release");
        att.setValue(release);
        sample.getAttributes().add(att);
        att = new Attribute();
        att.setName("update");
        att.setValue(update);
        sample.getAttributes().add(att);

        sample.setSampleRelationships(relationships);
        return sample;
    }

    public static Sample generateSample(String alias, String release) {
        Sample sample = new Sample();
        sample.setId(UUID.randomUUID().toString());
        sample.setAlias(alias);
        Attribute att = new Attribute();
        att.setName("release");
        att.setValue(release);
        sample.getAttributes().add(att);
        att = new Attribute();
        att.setName("update");
        att.setValue(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        sample.getAttributes().add(att);

        return sample;
    }

    public static SampleRelationship generateSampleRelationship(String accession, String nature) {
        SampleRelationship relationship = new SampleRelationship();
        relationship.setAccession(accession);
        relationship.setRelationshipNature(nature);
        return relationship;
    }

    public static ValidationMessageEnvelope generateValidationMessageEnvelope(Sample sample) {
        return new ValidationMessageEnvelope(
                UUID.randomUUID().toString(),
                1,
                sample
        );
    }
}
