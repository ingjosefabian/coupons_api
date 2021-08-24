package api.meli.com.co.infrastructure.exceptions;

/**
 * This exception is thrown when you get an error from the remote service
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
public class RemoteServiceException extends RuntimeException {

	public RemoteServiceException(final Throwable cause) {
		super(cause);
	}
}