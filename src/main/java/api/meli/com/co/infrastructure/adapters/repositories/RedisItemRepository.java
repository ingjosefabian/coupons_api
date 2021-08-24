package api.meli.com.co.infrastructure.adapters.repositories;

import api.meli.com.co.domain.repositories.ItemRepository;
import api.meli.com.co.domain.vo.Item;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

/**
 * This repository allows to obtain an item given its id from the Redis data store
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@Slf4j
@AllArgsConstructor
public class RedisItemRepository implements ItemRepository {

	/**
	 * This client allows to get redis values
	 */
	private final ReactiveStringRedisTemplate redisTemplate;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletionStage<Optional<Item>> findItemById(final String id) {
		log.info("Searching item value for item id: [{}] from cache", id);
		return redisTemplate.opsForValue().get(id)
			.map(value -> {
				if (value == null) {
					return buildNotFoundResponse(id);
				}
				log.info("The item : [{}] was obtained from cache", id);
				return Optional.of(Item.create(id, new BigDecimal(value)));
			})
			.onErrorResume(error -> Mono.just(buildErrorResponse(id, error)))
			.defaultIfEmpty(buildNotFoundResponse(id))
			.toFuture();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletionStage<Item> persistItem(final Item item) {
		log.info("Saving item value for item id: [{}] on cache", item.getId());
		return redisTemplate
			.opsForValue().set(item.getId(), item.getPrice().toString())
			.map(response -> item).toFuture();
	}

	private Optional<Item> buildNotFoundResponse(final String id) {
		log.info("The item:[{}] was not found in cache", id);
		return Optional.empty();
	}

	private Optional<Item> buildErrorResponse(final String id, final Throwable error) {
		log.error("Error getting item :[{}] from cache", id, error.getCause());
		return Optional.empty();
	}
}
