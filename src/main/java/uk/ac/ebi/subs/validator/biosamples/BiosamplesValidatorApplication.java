package uk.ac.ebi.subs.validator.biosamples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "uk.ac.ebi.subs.validator")
public class BiosamplesValidatorApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(BiosamplesValidatorApplication.class);
        ApplicationPidFileWriter applicationPidFileWriter = new ApplicationPidFileWriter();
        springApplication.addListeners( applicationPidFileWriter );
        springApplication.run(args);
    }
}
