package jcorechat.app_api;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class API {

    public static void main(String[] args) {


        System.out.println("App API");

        SpringApplication app = new SpringApplication(API.class);
        app.run(args);
    }
}