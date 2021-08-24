package api.meli.com.co.domain.aggregate;

import api.meli.com.co.domain.exceptions.InvalidAmountException;
import api.meli.com.co.domain.services.ItemFinderService;
import api.meli.com.co.domain.services.KnapsackCouponCalculator;
import api.meli.com.co.domain.vo.CalculatedCoupon;
import api.meli.com.co.domain.vo.Item;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;

/**
 * This add-on exposes the functionality for calculating the profit maximization of a coupon given a
 * list of purchase items.
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
public class CouponAggregate {

	private final ItemFinderService itemService;
	private BigDecimal couponAmount;
	private List<String> itemsToValidate;

	private CouponAggregate(final ItemFinderService itemService) {
		this.itemService = itemService;
	}

	public static CouponAggregate create(final ItemFinderService itemService) {
		return new CouponAggregate(itemService);
	}

	/**
	 * Add coupon value
	 *
	 * @param couponAmount the coupn value
	 */
	public void addCouponAmount(final BigDecimal couponAmount) {
		if (BigDecimal.ZERO.compareTo(couponAmount) >= 0) {
			throw new InvalidAmountException("Coupon value cannot be negative");
		}
		this.couponAmount = couponAmount;
	}

	/**
	 * Add the set of items to be analyzed
	 *
	 * @param items the item ids
	 */
	public void addItems(final List<String> items) {
		itemsToValidate = CollectionUtils.isNotEmpty(items) ? items : new ArrayList<>();
	}

	/**
	 * Calculate the maximum profit that can be obtained having the items and the value of the
	 * coupon
	 *
	 * @return list of items that can be purchased and max value to used
	 */
	public CompletionStage<CalculatedCoupon> calculateMaximumBenefit() {
		return itemService.findPricesByItemIds(itemsToValidate)
			.thenApply(items -> {
					final Stream<Item> filteredItems = items.stream()
						.filter(item -> isLessOrEqualThanCoupon(item.getPrice()));
					final KnapsackCouponCalculator calculator = KnapsackCouponCalculator
						.create(filteredItems.toArray(Item[]::new), couponAmount);
					return calculator.calculateMaximumBenefit();
				}
			);
	}

	private boolean isLessOrEqualThanCoupon(final BigDecimal price) {
		return price != null && couponAmount.compareTo(price) >= 0;
	}
}
