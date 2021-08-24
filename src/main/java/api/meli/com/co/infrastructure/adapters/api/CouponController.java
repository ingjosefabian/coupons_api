package api.meli.com.co.infrastructure.adapters.api;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Predicates.instanceOf;

import api.meli.com.co.application.CouponUseCases;
import api.meli.com.co.domain.exceptions.InvalidAmountException;
import api.meli.com.co.domain.exceptions.ItemsNotFoundException;
import api.meli.com.co.infrastructure.adapters.api.contracts.CouponRequest;
import api.meli.com.co.infrastructure.adapters.api.contracts.CouponResponse;
import java.math.BigDecimal;
import java.security.Principal;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;


/**
 * This controller allows the handling of requests associated with the coupon flow
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/coupon")
@AllArgsConstructor
public class CouponController {

	/**
	 * Use cases associated with the coupon flow
	 */
	private final CouponUseCases couponUseCases;

	/**
	 * This endpoint allows you to calculate the maximum benefit for a customer's coupon given a
	 * list of products
	 *
	 * @param request the coupon request with coupon and items
	 * @param principal the principal with auth
	 *
	 * @return the  maximum benefit response
	 */
	@PostMapping
	public Mono<ResponseEntity<CouponResponse>> calculateItemsToBuy(
		@Valid @RequestBody final CouponRequest request, final Mono<Principal> principal) {

		return principal
			.map(Principal::getName)
			.map(user -> {
				log.info(
					"A request has been received to calculate the maximum coupon benefit for the user: [{}] with coupon value: [{}]",
					user, request.getAmount());

				return couponUseCases
					.calculateMaximumBenefit(request.getAmount(), request.getItemIds());
			})
			.flatMap(Mono::fromCompletionStage)
			.map(coupon -> new CouponResponse(coupon.getAppliedItems(), coupon.getMaximumBenefit()))
			.map(ResponseEntity::ok)
			.onErrorResume(error -> Mono.just(buildResponseFromError(error, request.getAmount())));
	}

	private ResponseEntity<CouponResponse> buildResponseFromError(final Throwable error,
		final BigDecimal amount) {

		return Match(error).of(
			Case($(instanceOf(ItemsNotFoundException.class)), () -> {
				log.warn(
					"No items were found to apply a purchase for coupon value : [{}]", amount);
				return ResponseEntity.notFound().build();
			}),
			Case($(instanceOf(InvalidAmountException.class)), () -> {
				log.warn("Invalid  amount : [{}]", amount);
				return ResponseEntity.badRequest().build();
			}),
			Case($(), () -> {
				log.error(
					"Error calculating the maximum profit for the coupon of value: [{}], error message:[{}]",
					amount, error.getMessage(), error.getCause(), error);
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
			})
		);
	}
}
