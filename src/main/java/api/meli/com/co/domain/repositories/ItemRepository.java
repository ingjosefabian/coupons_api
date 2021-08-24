package api.meli.com.co.domain.repositories;

import api.meli.com.co.domain.vo.Item;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * This repository manages the storage of the items
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
public interface ItemRepository {

	/**
	 * Get an item given its id from the data store
	 *
	 * @param id the item id
	 *
	 * @return the found item
	 */
	CompletionStage<Optional<Item>> findItemById(String id);

	/**
	 * Persist item on storage
	 *
	 * @param item the item to save
	 *
	 * @return the item persisted
	 */
	CompletionStage<Item> persistItem(final Item item);
}
