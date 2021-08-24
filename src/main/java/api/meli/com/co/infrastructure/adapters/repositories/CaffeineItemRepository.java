package api.meli.com.co.infrastructure.adapters.repositories;

import api.meli.com.co.domain.repositories.ItemRepository;
import api.meli.com.co.domain.vo.Item;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * This repository allows to get item prices from caffeine
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@Slf4j
public class CaffeineItemRepository implements ItemRepository {

	/**
	 * The caffeine cache
	 */
	private final Cache<String, Float> cache;

	/**
	 * Default constructor
	 *
	 * @param timeToLife the expire after write value
	 * @param maximumSize the maximum size value
	 */
	public CaffeineItemRepository(final Integer timeToLife, final Integer maximumSize) {
		cache = Caffeine.newBuilder()
			.expireAfterWrite(timeToLife, TimeUnit.MINUTES)
			.maximumSize(maximumSize)
			.build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletionStage<Optional<Item>> findItemById(final String id) {
		log.info("Searching item value for item id: [{}] from cache", id);
		return CompletableFuture.supplyAsync(() -> cache.getIfPresent(id))
			.thenApply(value -> {
				if (value == null) {
					return buildNotFoundResponse(id);
				}
				log.info("The item : [{}] was obtained from cache", id);
				return Optional.of(Item.create(id, new BigDecimal(value)));
			})
			.exceptionally(error -> buildErrorResponse(id, error));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletionStage<Item> persistItem(final Item item) {
		return CompletableFuture
			.supplyAsync(() -> {
				log.info("Saving item value for item id: [{}] on cache", item.getId());
				cache.put(item.getId(), item.getPrice().floatValue());
				return item;
			});
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
