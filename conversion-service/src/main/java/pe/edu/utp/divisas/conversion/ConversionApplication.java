package pe.edu.utp.divisas.conversion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import pe.edu.utp.divisas.service.ConversionService;

@SpringBootApplication
public class ConversionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConversionApplication.class, args);
    }

    @Bean
    ConversionService conversionService() {
        return new ConversionService();
    }
}
