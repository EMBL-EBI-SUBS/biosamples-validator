package uk.ac.ebi.subs.validator.biosamples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;

@SpringBootApplication
@ComponentScan(basePackages = "uk.ac.ebi.subs.validator")
public class BiosamplesValidatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiosamplesValidatorApplication.class, args);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new MappingJackson2MessageConverter();
    }
}
