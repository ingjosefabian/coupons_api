package api.meli.com.co.domain.vo;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * This value object describes a coupon that has been calculated and the list of items that apply.
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@Getter
@Builder
@AllArgsConstructor
public final class CalculatedCoupon {

	private final BigDecimal couponAmount;
	private final BigDecimal maximumBenefit;
	private final List<String> appliedItems;
}
