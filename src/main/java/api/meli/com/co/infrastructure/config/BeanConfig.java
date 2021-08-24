package api.meli.com.co.infrastructure.config;

import api.meli.com.co.application.CouponUseCases;
import api.meli.com.co.domain.repositories.ItemRepository;
import api.meli.com.co.domain.services.ItemFinderService;
import api.meli.com.co.infrastructure.adapters.repositories.CaffeineItemRepository;
import api.meli.com.co.infrastructure.adapters.repositories.RedisItemRepository;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * The bean configuration
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@Configuration
@EnableWebFlux
public class BeanConfig {

	@Bean
	public CouponUseCases couponUseCases(final ItemFinderService itemService) {
		return new CouponUseCases(itemService);
	}

	@Bean
	WebClient webClient(@Value("${api.clients.meli.items.url}") final String baseUrl) {
		return WebClient.create(baseUrl);
	}

	@Bean
	public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
		final ReactiveRedisConnectionFactory factory) {
		return new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());
	}

	@Bean
	public Caffeine caffeineConfig() {
		return Caffeine.newBuilder().expireAfterWrite(60, TimeUnit.MINUTES);
	}

	@Bean
	public CacheManager cacheManager(final Caffeine caffeine) {
		final CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
		caffeineCacheManager.setCaffeine(caffeine);
		return caffeineCacheManager;
	}

	@Bean
	public ItemRepository itemRepository(
		@Value("${api.clients.cache.redis.enable}") final boolean redisEnable,
		@Value("${api.clients.cache.memory.time-to-life:20}") final Integer timeToLife,
		@Value("${api.clients.cache.memory.maximum-size:10}") final Integer maximumSize,
		final ReactiveStringRedisTemplate redisTemplate) {

		return redisEnable ?
			new RedisItemRepository(redisTemplate) :
			new CaffeineItemRepository(timeToLife, maximumSize);
	}
}
