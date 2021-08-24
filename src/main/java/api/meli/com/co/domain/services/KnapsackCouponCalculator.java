package api.meli.com.co.domain.services;

import api.meli.com.co.domain.vo.CalculatedCoupon;
import api.meli.com.co.domain.vo.Item;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * This service allows you to calculate the maximum benefit of the coupon given a set of items. To
 * perform this calculation, the backpack problem model is used
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@Slf4j
public class KnapsackCouponCalculator {

	/**
	 * List of elements to be analyzed
	 */
	private final Item[] items;
	/**
	 * the coupon amount
	 */
	private final int couponAmount;
	/**
	 * The optimal total benefit matrix for the first n items with total price
	 */
	private final int[][] bestValues;
	/**
	 * Composition of the first n elements, the optimal solution with a total amount
	 */
	private final List<Item> bestSolution;

	private KnapsackCouponCalculator(final Item[] items, final int couponAmount) {
		this.items = items;
		this.couponAmount = couponAmount;
		bestValues = new int[items.length + 1][couponAmount + 1];
		bestSolution = new ArrayList<>();
	}

	public static KnapsackCouponCalculator create(final Item[] items,
		final BigDecimal couponAmount) {
		return new KnapsackCouponCalculator(items, couponAmount.intValue());
	}

	/**
	 * Solve the backpack problem with the first n backpacks with a given total weight of total
	 * weight
	 */
	public CalculatedCoupon calculateMaximumBenefit() {

		log.info(
			"Starting analysis to calculate the maximum benefit for the coupon with amount: [{}] and items: {}",
			couponAmount, items);

		fillBestValues();
		updateBestSolution();

		final int maxBenefit = bestValues[items.length][couponAmount];
		final List<String> itemsApplied = bestSolution.stream().map(Item::getId)
			.collect(Collectors.toList());

		log.info(
			"The analysis for the calculation of the maximum benefit for the coupon with amount: [{}], and items: {} has been completed. The maximum profit found is: [{}], adding the items: {}",
			couponAmount, items, maxBenefit, itemsApplied);

		return CalculatedCoupon.builder()
			.couponAmount(BigDecimal.valueOf(couponAmount))
			.maximumBenefit(BigDecimal.valueOf(maxBenefit))
			.appliedItems(itemsApplied)
			.build();
	}

	private void fillBestValues() {
		for (int j = 0; j <= couponAmount; j++) {
			for (int i = 0; i <= items.length; i++) {
				if (i == 0 || j == 0) {
					bestValues[i][j] = 0;
				} else {
					// If the weight of item i-th is greater than the total weight, the optimal solution exists in the first items i-1,
					if (j < items[i - 1].getRoundedPrice()) {
						bestValues[i][j] = bestValues[i - 1][j];
					} else {
						// If item i-th is not greater than the total weight, the optimal solution is the optimal solution that item i-th contains,
						// Or the optimal solution that does not contain the i-th item, whichever is maximum, and the classification discussion method is used
						final int iWeight = items[i - 1].getRoundedPrice();
						final int iValue = items[i - 1].getRoundedPrice();
						bestValues[i][j] = Math
							.max(bestValues[i - 1][j], iValue + bestValues[i - 1][j - iWeight]);
					}
				}
			}
		}
	}

	private void updateBestSolution() {
		int tempWeight = couponAmount;
		for (int i = items.length; i >= 1; i--) {
			if (bestValues[i][tempWeight] > bestValues[i - 1][tempWeight]) {
				bestSolution.add(items[i - 1]);
				tempWeight -= items[i - 1].getRoundedPrice();
			}
			if (tempWeight == 0) {
				break;
			}
		}
	}
}
