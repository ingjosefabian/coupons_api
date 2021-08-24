package api.meli.com.co.infrastructure.adapters.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import api.meli.com.co.application.CouponUseCases;
import api.meli.com.co.domain.exceptions.InvalidAmountException;
import api.meli.com.co.domain.exceptions.ItemsNotFoundException;
import api.meli.com.co.domain.vo.CalculatedCoupon;
import api.meli.com.co.infrastructure.adapters.api.contracts.CouponRequest;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * The test cases for {@link CouponController}
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
public class CouponControllerTest {

	@Mock
	private CouponUseCases couponUseCases;

	@InjectMocks
	private CouponController controller;

	@Test
	public void calculateItemsToBuy_whenCouponIsValidAndThereAreItemsToBuy() {
		when(couponUseCases
			.calculateMaximumBenefit(Mockito.any(BigDecimal.class), Mockito.anyList()))
			.thenReturn(CompletableFuture.completedFuture(CalculatedCoupon.builder()
				.couponAmount(BigDecimal.valueOf(500))
				.appliedItems(Arrays.asList("M1", "M2", "M3", "M4"))
				.maximumBenefit(BigDecimal.valueOf(490))
				.build()));

		StepVerifier.create(controller
			.calculateItemsToBuy(buildRequest(BigDecimal.valueOf(500)), Mono.just(() -> "client1")))
			.consumeNextWith(response -> {
				assertThat(response.getStatusCodeValue(), is(200));
				assertThat(response.getBody().getTotal(), is(BigDecimal.valueOf(490)));
				assertThat(response.getBody().getItemIds(),
					Matchers.containsInAnyOrder("M1", "M2", "M3", "M4"));
			}).verifyComplete();

		verify(couponUseCases)
			.calculateMaximumBenefit(Mockito.any(BigDecimal.class), Mockito.anyList());
	}

	@Test
	public void calculateItemsToBuy_whenCouponIsValidAndThereAreNotItemsToBuy() {
		when(couponUseCases
			.calculateMaximumBenefit(Mockito.any(BigDecimal.class), Mockito.anyList()))
			.thenReturn(CompletableFuture.supplyAsync(() -> {
				throw new ItemsNotFoundException();
			}));

		StepVerifier.create(controller
			.calculateItemsToBuy(buildRequest(BigDecimal.valueOf(200)), Mono.just(() -> "client1")))
			.consumeNextWith(response -> {
				assertThat(response.getStatusCodeValue(), is(404));
				assertThat(response.getBody(), is(nullValue()));
			}).verifyComplete();

		verify(couponUseCases)
			.calculateMaximumBenefit(Mockito.any(BigDecimal.class), Mockito.anyList());
	}

	@Test
	public void calculateItemsToBuy_whenCouponIsInValid() {
		when(couponUseCases
			.calculateMaximumBenefit(Mockito.any(BigDecimal.class), Mockito.anyList()))
			.thenReturn(CompletableFuture.supplyAsync(() -> {
				throw new InvalidAmountException("invalid coupon");
			}));

		StepVerifier.create(controller.calculateItemsToBuy(buildRequest(BigDecimal.valueOf(-100)),
			Mono.just(() -> "client1")))
			.consumeNextWith(response -> {
				assertThat(response.getStatusCodeValue(), is(400));
				assertThat(response.getBody(), is(nullValue()));
			}).verifyComplete();

		verify(couponUseCases)
			.calculateMaximumBenefit(Mockito.any(BigDecimal.class), Mockito.anyList());
	}

	@Test
	public void calculateItemsToBuy_whenUseCaseResponseError() {
		when(couponUseCases
			.calculateMaximumBenefit(Mockito.any(BigDecimal.class), Mockito.anyList()))
			.thenReturn(CompletableFuture.supplyAsync(() -> {
				throw new IllegalArgumentException("error");
			}));

		StepVerifier.create(controller.calculateItemsToBuy(buildRequest(BigDecimal.valueOf(-100)),
			Mono.just(() -> "client1")))
			.consumeNextWith(response -> {
				assertThat(response.getStatusCodeValue(), is(503));
				assertThat(response.getBody(), is(nullValue()));
			}).verifyComplete();

		verify(couponUseCases)
			.calculateMaximumBenefit(Mockito.any(BigDecimal.class), Mockito.anyList());
	}

	private CouponRequest buildRequest(final BigDecimal amount) {
		return new CouponRequest(amount, Arrays.asList("M1", "M2", "M3", "M4", "M5"));
	}
}
