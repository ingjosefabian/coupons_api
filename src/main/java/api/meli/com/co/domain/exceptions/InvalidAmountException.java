package api.meli.com.co.domain.exceptions;

/**
 * This exception is fired when trying to apply a negative value
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
public class InvalidAmountException extends RuntimeException {

	public InvalidAmountException(final String message) {
		super(message);
	}
}
