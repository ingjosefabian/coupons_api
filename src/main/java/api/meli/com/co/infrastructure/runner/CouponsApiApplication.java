package api.meli.com.co.infrastructure.runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The class runner
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@SpringBootApplication(scanBasePackages = "api.meli.com.co.infrastructure")
public class CouponsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CouponsApiApplication.class, args);
	}
}
