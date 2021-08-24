package api.meli.com.co.infrastructure.adapters.api.contracts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This response contains the items that can be purchased given the amount of the coupon and the
 * total purchase that can be made
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CouponResponse implements Serializable {

	@JsonProperty("item_ids")
	private List<String> itemIds;
	private BigDecimal total;
}
