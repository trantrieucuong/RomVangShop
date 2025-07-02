package vn.fs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RomvangShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(RomvangShopApplication.class, args);
	}

}
