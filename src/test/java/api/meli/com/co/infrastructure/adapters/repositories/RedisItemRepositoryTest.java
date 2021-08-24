package api.meli.com.co.infrastructure.adapters.repositories;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import api.meli.com.co.domain.vo.Item;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

/**
 * The test cases for {@link RedisItemRepository}
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@ExtendWith({MockitoExtension.class})
public class RedisItemRepositoryTest {

	private final ReactiveValueOperations valueOperations = Mockito
		.mock(ReactiveValueOperations.class);
	@Mock
	private ReactiveStringRedisTemplate redisTemplate;
	@InjectMocks
	private RedisItemRepository repository;

	@BeforeEach
	public void setUpdate() {
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	@Test
	public void findItemById_whenItemExistInCache() throws Exception {

		when(valueOperations.get(Mockito.anyString()))
			.thenReturn(Mono.just("100"));

		final Optional<Item> response = repository.findItemById("m1").toCompletableFuture().get();

		assertThat(response.get().getId(), is("m1"));
		assertThat(response.get().getPrice(), is(BigDecimal.valueOf(100)));

		verify(valueOperations).get(Mockito.anyString());
		verify(redisTemplate).opsForValue();
	}

	@Test
	public void findItemById_whenItemNotExistInCache() throws Exception {

		when(valueOperations.get(Mockito.anyString()))
			.thenReturn(Mono.empty());

		final Optional<Item> response = repository.findItemById("m2").toCompletableFuture().get();

		assertThat(response.isPresent(), is(false));

		verify(valueOperations).get(Mockito.anyString());
		verify(redisTemplate).opsForValue();
	}

	@Test
	public void findItemById_whenClientResponseWithError() throws Exception {

		when(valueOperations.get(Mockito.anyString()))
			.thenReturn(Mono.error(new IllegalArgumentException("Cache error")));

		final Optional<Item> response = repository.findItemById("m3").toCompletableFuture().get();

		assertThat(response.isPresent(), is(false));

		verify(valueOperations).get(Mockito.anyString());
		verify(redisTemplate).opsForValue();
	}

	@Test
	public void persistItem_whenClientSuccessResponse() throws Exception {
		when(valueOperations.set(Mockito.anyString(), Mockito.anyString()))
			.thenReturn(Mono.just(true));

		final Item response = repository.persistItem(Item.create("m1", BigDecimal.TEN))
			.toCompletableFuture().get();
		assertThat(response.getId(), is("m1"));
		assertThat(response.getPrice(), is(BigDecimal.TEN));

		verify(valueOperations).set(Mockito.anyString(), Mockito.anyString());
		verify(redisTemplate).opsForValue();
	}

	@Test
	public void persistItem_whenClientErrorResponse() throws Exception {
		when(valueOperations.set(Mockito.anyString(), Mockito.anyString()))
			.thenReturn(Mono.error(new IllegalArgumentException("error")));

		final Throwable error = Assertions.assertThrows(Throwable.class,
			() -> repository.persistItem(Item.create("m1", BigDecimal.TEN))
				.toCompletableFuture().get());

		assertThat(error.getCause(), instanceOf(IllegalArgumentException.class));

		verify(valueOperations).set(Mockito.anyString(), Mockito.anyString());
		verify(redisTemplate).opsForValue();
	}
}