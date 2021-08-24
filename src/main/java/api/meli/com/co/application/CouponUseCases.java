package api.meli.com.co.application;

import api.meli.com.co.domain.aggregate.CouponAggregate;
import api.meli.com.co.domain.exceptions.ItemsNotFoundException;
import api.meli.com.co.domain.services.ItemFinderService;
import api.meli.com.co.domain.vo.CalculatedCoupon;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletionStage;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Contains the use cases associated with the coupon flow
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@AllArgsConstructor
public class CouponUseCases {

	/**
	 * This service allows to orchestrate the logic of the item query flow
	 */
	private final ItemFinderService itemService;

	/**
	 * Get the list of items that can be purchased given the coupon amount
	 *
	 * @param coupon The coupon amount
	 * @param items list of items where you want to perform the analysis
	 *
	 * @return list of items that can be purchased
	 */
	public CompletionStage<CalculatedCoupon> calculateMaximumBenefit(final BigDecimal coupon,
		final List<String> items) {

		final CouponAggregate couponAggregate = CouponAggregate.create(itemService);
		couponAggregate.addCouponAmount(coupon);
		couponAggregate.addItems(items);

		return couponAggregate.calculateMaximumBenefit()
			.thenApply(response -> {
				if (CollectionUtils.isEmpty(response.getAppliedItems())) {
					throw new ItemsNotFoundException();
				}
				return response;
			});
	}
}
