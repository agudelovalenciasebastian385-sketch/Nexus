package nexusmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * NexusMarket application entry point.
 *
 * <p>Boots the Spring application and loads the infrastructure that surrounds
 * the pure domain layer: REST input adapters, the MySQL persistence output
 * adapter and the security configuration (RG-01).</p>
 *
 * <p>The root package {@code nexusmarket} organizes the architectural
 * components defined in the Software Architecture document:
 * {@code adapters}, {@code domain} and {@code infrastructure}.</p>
 */
@SpringBootApplication
public class App {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

}
