package nexusmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

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
 *   <li>{@code adapters.persistence}, {@code adapters.security} and
 *       {@code adapters.payment}: the implementations of the 16 output ports
 *       (MySQL, MongoDB, BCrypt, JWT and the simulated payment gateway).</li>
 *   <li>{@code infrastructure.seed}: the initial Administrator seeder.</li>
 * </ul>
 *
 * <p>{@code adapters.rest} exposes the role ports over HTTP and
 * {@code adapters.security} secures them with JWT (RG-01). Authentication is
 * JWT-only, so Spring Boot's in-memory user (UserDetailsServiceAutoConfiguration,
 * which prints a generated password) is excluded.</p>
 *
 * <p>The package structure follows the Software Architecture document.</p>
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class App {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

}
