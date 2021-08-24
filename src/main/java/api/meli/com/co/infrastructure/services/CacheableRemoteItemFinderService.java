package api.meli.com.co.infrastructure.services;

import api.meli.com.co.domain.repositories.ItemRepository;
import api.meli.com.co.domain.services.ItemFinderService;
import api.meli.com.co.domain.vo.Item;
import api.meli.com.co.infrastructure.adapters.clients.MeliItemRestClient;
import java.util.List;
import java.util.concurrent.CompletionStage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This service allows the query of items by implementing port XX and saving the results in cache.
 * First look for the results in cache and if no results are found then invoke the remote service
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@Slf4j
@Service
@AllArgsConstructor
public class CacheableRemoteItemFinderService implements ItemFinderService {

	/**
	 * Rest client to find item information from Meli Api
	 */
	private final MeliItemRestClient meliItemRestClient;

	/**
	 * Cache repository to find item values
	 */
	private final ItemRepository itemRepository;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CompletionStage<List<Item>> findPricesByItemIds(final List<String> itemIds) {
		return Flux.fromIterable(itemIds)
			.flatMap(this::fromCache)
			.flatMap(this::fromRemoteServiceIfNecessary)
			.map(item -> {
				log.info("The item with id :[{}] was found with price [{}]", item.getId(),
					item.getPrice());
				return item;
			})
			.onErrorContinue((error, item) ->
				log.error("Error getting item :[{}], error message:[{}]",
					item, error.getMessage(), error)
			).collectList().toFuture();
	}

	private Mono<Item> fromCache(final String itemId) {
		return Mono
			.fromCompletionStage(itemRepository.findItemById(itemId))
			.map(item -> item.orElse(Item.create(itemId)));
	}

	private Mono<Item> fromRemoteServiceIfNecessary(final Item item) {
		if (item.getPrice() == null) {
			return meliItemRestClient.getItemPriceById(item.getId())
				.flatMap(result -> Mono.fromCompletionStage(itemRepository.persistItem(result)));
		}
		return Mono.just(item);
	}
}
