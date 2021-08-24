package api.meli.com.co.infrastructure.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * The api security configuration
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@AllArgsConstructor
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

	/**
	 * Role for basic authentication.
	 */
	private static final String ROLE_USER = "USER";

	/**
	 * Properties of usernames and passwords for basic authentication.
	 */
	private final BasicAuthenticationPropertiesConfig basicAuthenticationProps;

	/**
	 * Endpoint security configuration1
	 *
	 * @param http the {@link ServerHttpSecurity} object
	 *
	 * @return the {@link SecurityWebFilterChain} reference object
	 */
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(final ServerHttpSecurity http) {
		return http.csrf().disable()
			.authorizeExchange()
			.pathMatchers("/monitoring/**").permitAll()
			.anyExchange().authenticated().and().httpBasic()
			.and().build();
	}

	/**
	 * Register user credentials on security context
	 *
	 * @param encoder the encoder to save the passwd
	 *
	 * @return instance of {@link MapReactiveUserDetailsService}
	 */
	@Bean
	public MapReactiveUserDetailsService userDetailsService(final PasswordEncoder encoder) {
		return new MapReactiveUserDetailsService(User.builder()
			.username(basicAuthenticationProps.getUser())
			.password(encoder.encode(basicAuthenticationProps.getPass()))
			.roles(ROLE_USER).build()
		);
	}

	/**
	 * Define the encoder to security context
	 *
	 * @return instance of {@link PasswordEncoder}
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
