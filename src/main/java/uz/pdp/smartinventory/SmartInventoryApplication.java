package uz.pdp.smartinventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SmartInventoryApplication {

    public static void main(String[] args) {

        SpringApplication.run(SmartInventoryApplication.class, args);
    }

}
