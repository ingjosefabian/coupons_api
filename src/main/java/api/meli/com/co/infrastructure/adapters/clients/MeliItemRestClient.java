package api.meli.com.co.infrastructure.adapters.clients;

import api.meli.com.co.domain.vo.Item;
import api.meli.com.co.infrastructure.exceptions.RemoteServiceException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * This client service allows to get item prices from remote Meli api
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@Slf4j
@Component
public class MeliItemRestClient {

	/**
	 * Web client to interact with the Meli Items Api
	 */
	private final WebClient webClient;

	/**
	 * The circuit breaker factory
	 */
	private final ReactiveCircuitBreakerFactory circuitBreakerFactory;

	/**
	 * The item client timeout
	 */
	private final int timeout;

	public MeliItemRestClient(final WebClient webClient,
		final ReactiveCircuitBreakerFactory circuitBreakerFactory,
		@Value("${api.clients.meli.items.timeout}") final int timeout) {

		this.webClient = webClient;
		this.circuitBreakerFactory = circuitBreakerFactory;
		this.timeout = timeout;
	}

	/**
	 * Obtain the information of a purchase item from the item id
	 *
	 * @param itemId the item id to get
	 *
	 * @return the item information
	 */
	public Mono<Item> getItemPriceById(final String itemId) {
		log.info("Searching information for the item id: [{}] from the remote items service",
			itemId);
		return webClient.get()
			.uri(itemId)
			.exchangeToMono(response -> response.bodyToMono(GetItemResponse.class))
			.timeout(Duration.ofMillis(timeout))
			.transform(it ->
				circuitBreakerFactory.create("items").run(it, throwable -> {
					log.error("Error getting item :[{}] from remote service", itemId,
						throwable.getCause());
					return Mono.error(new RemoteServiceException(throwable));
				})
			)
			.map(response -> Item.create(response.getId(), response.getPrice()));
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class GetItemResponse implements Serializable {

		private String id;
		private BigDecimal price;
	}
}

