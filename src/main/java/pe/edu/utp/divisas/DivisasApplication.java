package pe.edu.utp.divisas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class DivisasApplication {

    public static void main(String[] args) {
        SpringApplication.run(DivisasApplication.class, args);
    }
}
