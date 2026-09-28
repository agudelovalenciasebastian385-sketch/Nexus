package nexusmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * NexusMarket application entry point.
 *
 * <p>Boots the Spring application and registers, through component scanning,
 * the components that exist today:</p>
 * <ul>
 *   <li>{@code domain}: models, value objects, exceptions, the domain services
 *       ({@code domain.services}) and the input and output ports
 *       ({@code domain.ports.in}, {@code domain.ports.out}).</li>
 *   <li>{@code adapters.useCases}: the implementations of the role ports, which
 *       delegate to the domain services.</li>
 * </ul>
 *
 * <p>Next phase (not implemented yet): REST input adapters, the MySQL and
 * MongoDB persistence adapters, the password, JWT and payment gateway adapters
 * that implement the output ports, and the security configuration (RG-01).
 * Until those output adapters exist the Spring context cannot start, because
 * no bean implements the output ports.</p>
 *
 * <p>The package structure follows the Software Architecture document.</p>
 */
@SpringBootApplication
public class App {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

}
