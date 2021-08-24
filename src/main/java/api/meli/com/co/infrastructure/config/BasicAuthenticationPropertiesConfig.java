package api.meli.com.co.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * This configuration class allows to load the session credentials from the properties file
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "api.auth.basic")
public class BasicAuthenticationPropertiesConfig {

	private String user;
	private String pass;
}
