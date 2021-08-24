package api.meli.com.co.domain.services;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import api.meli.com.co.domain.vo.CalculatedCoupon;
import api.meli.com.co.domain.vo.Item;
import java.math.BigDecimal;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;


/**
 * The test cases for {@link KnapsackCouponCalculator}
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
public class KnapsackCouponCalculatorTest {

	@Test
	public void calculateMaximumBenefit_whenMaxBenefitIs480() {
		final Item[] items = new Item[]{
			Item.create("m1", BigDecimal.valueOf(100)),
			Item.create("m2", BigDecimal.valueOf(210)),
			Item.create("m3", BigDecimal.valueOf(260)),
			Item.create("m4", BigDecimal.valueOf(80)),
			Item.create("m5", BigDecimal.valueOf(90))
		};
		final KnapsackCouponCalculator couponKnapsackCalculator = KnapsackCouponCalculator
			.create(items, BigDecimal.valueOf(500));

		final CalculatedCoupon response = couponKnapsackCalculator.calculateMaximumBenefit();
		assertThat(response.getMaximumBenefit().intValue(), is(480));
		assertThat(
			response.getAppliedItems(), Matchers.containsInAnyOrder("m1", "m2", "m4", "m5"));
	}

	@Test
	public void calculateMaximumBenefit_whenMaxBenefitIs700() {
		final Item[] items = new Item[]{
			Item.create("m1", BigDecimal.valueOf(100)),
			Item.create("m2", BigDecimal.valueOf(155)),
			Item.create("m3", BigDecimal.valueOf(50)),
			Item.create("m4", BigDecimal.valueOf(112)),
			Item.create("m5", BigDecimal.valueOf(70)),
			Item.create("m6", BigDecimal.valueOf(80)),
			Item.create("m7", BigDecimal.valueOf(60)),
			Item.create("m8", BigDecimal.valueOf(118)),
			Item.create("m9", BigDecimal.valueOf(110)),
			Item.create("m10", BigDecimal.valueOf(55))
		};
		final KnapsackCouponCalculator couponKnapsackCalculator = KnapsackCouponCalculator.create(
			items, BigDecimal.valueOf(700));

		final CalculatedCoupon response = couponKnapsackCalculator.calculateMaximumBenefit();
		assertThat(response.getMaximumBenefit().intValue(), is(700));
		assertThat(response.getAppliedItems(),
			Matchers.containsInAnyOrder("m1", "m3", "m4", "m5", "m6", "m7", "m8", "m9"));
	}

	@Test
	public void calculateMaximumBenefit_whenMaxBenefitIs5() {
		final Item[] items = new Item[]{
			Item.create("m1", BigDecimal.valueOf(2)),
			Item.create("m2", BigDecimal.valueOf(3)),
			Item.create("m3", BigDecimal.valueOf(4)),
			Item.create("m4", BigDecimal.valueOf(5))
		};
		final KnapsackCouponCalculator couponKnapsackCalculator = KnapsackCouponCalculator
			.create(items, BigDecimal.valueOf(5));

		final CalculatedCoupon response = couponKnapsackCalculator.calculateMaximumBenefit();
		assertThat(response.getMaximumBenefit().intValue(), is(5));
		assertThat(response.getAppliedItems(), Matchers.containsInAnyOrder("m1", "m2"));
	}

	@Test
	public void calculateMaximumBenefit_whenMaxBenefitIs59() {
		final Item[] items = new Item[]{
			Item.create("m1", BigDecimal.valueOf(42)),
			Item.create("m2", BigDecimal.valueOf(23)),
			Item.create("m3", BigDecimal.valueOf(21)),
			Item.create("m4", BigDecimal.valueOf(15)),
			Item.create("m5", BigDecimal.valueOf(7))
		};

		final KnapsackCouponCalculator couponKnapsackCalculator = KnapsackCouponCalculator
			.create(items, BigDecimal.valueOf(60));

		final CalculatedCoupon response = couponKnapsackCalculator.calculateMaximumBenefit();
		assertThat(response.getMaximumBenefit().intValue(), is(59));
		assertThat(response.getAppliedItems(), Matchers.containsInAnyOrder("m2", "m3", "m4"));
	}

	@Test
	public void calculateMaximumBenefit_whenMaxBenefitIs8() {
		final Item[] items = new Item[]{
			Item.create("m1", BigDecimal.valueOf(4)),
			Item.create("m2", BigDecimal.valueOf(3)),
			Item.create("m3", BigDecimal.valueOf(5)),
			Item.create("m4", BigDecimal.valueOf(2))

		};

		final KnapsackCouponCalculator couponKnapsackCalculator = KnapsackCouponCalculator
			.create(items, BigDecimal.valueOf(8));

		final CalculatedCoupon response = couponKnapsackCalculator.calculateMaximumBenefit();
		assertThat(response.getMaximumBenefit().intValue(), is(8));
		assertThat(response.getAppliedItems(), Matchers.containsInAnyOrder("m2", "m3"));
	}

	@Test
	public void calculateMaximumBenefit_whenMaxBenefitIs11() {
		final Item[] items = new Item[]{
			Item.create("m1", BigDecimal.valueOf(6)),
			Item.create("m2", BigDecimal.valueOf(4)),
			Item.create("m3", BigDecimal.valueOf(3)),
			Item.create("m4", BigDecimal.valueOf(2))

		};

		final KnapsackCouponCalculator couponKnapsackCalculator = KnapsackCouponCalculator
			.create(items, BigDecimal.valueOf(11));

		final CalculatedCoupon response = couponKnapsackCalculator.calculateMaximumBenefit();
		assertThat(response.getMaximumBenefit().intValue(), is(11));
		assertThat(response.getAppliedItems(), Matchers.containsInAnyOrder("m1", "m3", "m4"));
	}

}
