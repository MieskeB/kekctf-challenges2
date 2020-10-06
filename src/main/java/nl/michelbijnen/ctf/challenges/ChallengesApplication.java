package nl.michelbijnen.ctf.challenges;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class ChallengesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChallengesApplication.class, args);
    }

}
