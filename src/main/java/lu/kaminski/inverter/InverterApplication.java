package lu.kaminski.inverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InverterApplication {

    public static void main(String[] args) {
        SpringApplication.run(InverterApplication.class, args);
    }

}
