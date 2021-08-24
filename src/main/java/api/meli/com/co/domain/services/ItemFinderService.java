package api.meli.com.co.domain.services;

import api.meli.com.co.domain.vo.Item;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * This service exposes a port for consulting items and their prices
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
public interface ItemFinderService {

	/**
	 * This method allows you to search the prices of a list of products given the ids of said
	 * products
	 *
	 * @param itemIds the items id to get prices
	 *
	 * @return the prices of items
	 */
	CompletionStage<List<Item>> findPricesByItemIds(List<String> itemIds);
}
