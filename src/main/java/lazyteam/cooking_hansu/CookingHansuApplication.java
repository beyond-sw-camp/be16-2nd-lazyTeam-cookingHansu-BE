package lazyteam.cooking_hansu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CookingHansuApplication {

	public static void main(String[] args) {
		SpringApplication.run(CookingHansuApplication.class, args);
	}

}
