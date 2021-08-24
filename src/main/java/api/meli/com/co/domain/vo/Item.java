package api.meli.com.co.domain.vo;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Describes a purchase item
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Item {

	private final String id;
	private final BigDecimal price;

	public static Item create(final String id) {
		return new Item(id, null);
	}

	public static Item create(final String id, final BigDecimal price) {
		return new Item(id, price);
	}

	public int getRoundedPrice() {
		return price.intValue();
	}

	@Override
	public String toString() {
		return "{id:" + id + ", price:" + price + "}";
	}
}
