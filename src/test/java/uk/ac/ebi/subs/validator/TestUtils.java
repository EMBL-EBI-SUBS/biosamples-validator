package uk.ac.ebi.subs.validator;

import uk.ac.ebi.subs.data.component.Attribute;
import uk.ac.ebi.subs.data.component.SampleRelationship;
import uk.ac.ebi.subs.data.submittable.Sample;
import uk.ac.ebi.subs.validator.data.ValidationMessageEnvelope;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TestUtils {

    public static Sample generateSample(String alias) {
        return generateSample(alias, LocalDate.now());
    }

    public static Sample generateSample(String alias, LocalDate releaseDate, String update, List<SampleRelationship> relationships) {
        Sample sample = new Sample();
        sample.setId(UUID.randomUUID().toString());
        sample.setAlias(alias);
        sample.setReleaseDate(releaseDate);

        Attribute att = new Attribute();
        att.setValue(update);
        sample.getAttributes().put("update", Collections.singletonList(att));

        sample.setSampleRelationships(relationships);
        return sample;
    }

    public static Sample generateSample(String alias, LocalDate releaseDate) {
        Sample sample = new Sample();
        sample.setId(UUID.randomUUID().toString());
        sample.setAlias(alias);
        sample.setReleaseDate(releaseDate);

        Attribute att = new Attribute();
        att.setValue(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        sample.getAttributes().put("update", Collections.singletonList(att));

        return sample;
    }

    public static SampleRelationship generateSampleRelationship(String accession, String nature, String alias) {
        SampleRelationship relationship = new SampleRelationship();
        relationship.setAccession(accession);
        relationship.setRelationshipNature(nature);
        relationship.setAlias(alias);
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
