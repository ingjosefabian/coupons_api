package api.meli.com.co.infrastructure.adapters.api;

import api.meli.com.co.infrastructure.adapters.api.contracts.CouponRequest;
import api.meli.com.co.infrastructure.runner.CouponsApiApplication;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.math.BigDecimal;
import java.util.Arrays;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

/**
 * The integration test for {@Link CouponController}
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = CouponsApiApplication.class)
@AutoConfigureWireMock(port = 1200)
public class CouponControllerIntegrationTest {

	@Autowired
	private WireMockServer wireMockServer;

	@Autowired
	private WebTestClient webClient;

	@Test
	@DisplayName("should return a list of items to apply the purchase and the maximum amount that could be used from the coupon")
	public void calculateItemsToBuy_whenCouponIsValidAndThereAreItemsToBuy() {

		webClient
			.post()
			.uri("/api/coupon")
			.header("Authorization", "Basic bWVsaTp0ZXN0")
			.body(BodyInserters.fromValue(new CouponRequest(BigDecimal.valueOf(500), Arrays.asList(
				"ML1", "ML2", "ML3", "ML4", "ML5"
			))))
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.total").isEqualTo(BigDecimal.valueOf(480))
			.jsonPath("$.item_ids").value(Matchers.containsInAnyOrder("ML1", "ML2", "ML4", "ML5"));
	}

	@Test
	@DisplayName("should return a not found response")
	public void calculateItemsToBuy_whenCouponIsValidAndThereAreNotItemsToBuy() {

		webClient
			.post()
			.uri("/api/coupon")
			.header("Authorization", "Basic bWVsaTp0ZXN0")
			.body(BodyInserters.fromValue(new CouponRequest(BigDecimal.valueOf(70), Arrays.asList(
				"ML1", "ML2", "ML3"
			))))
			.exchange()
			.expectStatus().isNotFound();
	}
}
