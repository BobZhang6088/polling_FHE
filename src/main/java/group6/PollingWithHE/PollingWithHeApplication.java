package group6.PollingWithHE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class PollingWithHeApplication {

	public static void main(String[] args) {
		System.setProperty("jna.library.path", "/Users/chriszhang/Documents/Course material - NYIT Vancouver /870/PollingWithHE/build/resources/main/CPP");
		SpringApplication.run(PollingWithHeApplication.class, args);
	}
	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}

}
