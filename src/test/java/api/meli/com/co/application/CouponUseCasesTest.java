package api.meli.com.co.application;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import api.meli.com.co.domain.exceptions.ItemsNotFoundException;
import api.meli.com.co.domain.services.ItemFinderService;
import api.meli.com.co.domain.vo.CalculatedCoupon;
import api.meli.com.co.domain.vo.Item;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The test cases for {@link CouponUseCases}
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
public class CouponUseCasesTest {

	@Mock
	private ItemFinderService itemService;

	@InjectMocks
	private CouponUseCases useCases;

	@Test
	public void calculateMaximumBenefit_whenCouponIsValidAndApplyItems() throws Exception {
		when(itemService.findPricesByItemIds(Mockito.anyList()))
			.thenReturn(CompletableFuture.completedFuture(Arrays.asList(
				Item.create("MLA1", BigDecimal.valueOf(100)),
				Item.create("MLA2", BigDecimal.valueOf(210)),
				Item.create("MLA3", BigDecimal.valueOf(260)),
				Item.create("MLA4", BigDecimal.valueOf(80)),
				Item.create("MLA5", BigDecimal.valueOf(90))
			)));

		final CalculatedCoupon response = useCases
			.calculateMaximumBenefit(BigDecimal.valueOf(500),
				Arrays.asList("MLA1", "MLA2", "MLA3", "MLA4", "MLA5")).toCompletableFuture().get();

		assertThat(response.getCouponAmount(), is(BigDecimal.valueOf(500)));
		assertThat(response.getMaximumBenefit(), is(BigDecimal.valueOf(480)));
		assertThat(response.getAppliedItems(),
			Matchers.containsInAnyOrder("MLA1", "MLA2", "MLA4", "MLA5"));

		verify(itemService).findPricesByItemIds(Mockito.anyList());
	}

	@Test
	public void calculateMaximumBenefit_whenCouponIsValidAndNotApplyItems() throws Exception {
		when(itemService.findPricesByItemIds(Mockito.anyList()))
			.thenReturn(CompletableFuture.completedFuture(Arrays.asList(
				Item.create("MLA1", BigDecimal.valueOf(600)),
				Item.create("MLA2", BigDecimal.valueOf(610)),
				Item.create("MLA3", BigDecimal.valueOf(660)),
				Item.create("MLA4", BigDecimal.valueOf(680)),
				Item.create("MLA5", BigDecimal.valueOf(690))
			)));

		final Throwable error = Assertions.assertThrows(Exception.class, () ->
			useCases
				.calculateMaximumBenefit(BigDecimal.valueOf(500),
					Arrays.asList("MLA1", "MLA2", "MLA3", "MLA4", "MLA5")).toCompletableFuture()
				.get()
		);

		assertThat(error.getCause(), instanceOf(ItemsNotFoundException.class));

		verify(itemService).findPricesByItemIds(Mockito.anyList());
	}
}
