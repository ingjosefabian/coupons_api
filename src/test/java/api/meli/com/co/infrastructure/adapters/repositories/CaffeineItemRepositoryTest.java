package api.meli.com.co.infrastructure.adapters.repositories;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import api.meli.com.co.domain.vo.Item;
import com.github.benmanes.caffeine.cache.Cache;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The test cases for {@link CaffeineItemRepository}
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@ExtendWith({MockitoExtension.class})
public class CaffeineItemRepositoryTest {

	private Cache<String, Float> cache = Mockito.mock(Cache.class);
	private CaffeineItemRepository repository = new CaffeineItemRepository(10, 10);

	@BeforeEach
	public void setUp() throws Exception {
		reset(cache);
		final Field field = CaffeineItemRepository.class.getDeclaredField("cache");
		field.setAccessible(true);
		field.set(repository, cache);
	}

	@Test
	public void findItemById_whenItemExistInCache() throws Exception {
		when(cache.getIfPresent(Mockito.anyString())).thenReturn(100F);

		final Optional<Item> response = repository.findItemById("m1").toCompletableFuture().get();

		assertThat(response.get().getId(), is("m1"));
		assertThat(response.get().getPrice(), is(BigDecimal.valueOf(100)));

		verify(cache).getIfPresent(Mockito.anyString());
	}

	@Test
	public void findItemById_whenItemNotExistInCache() throws Exception {
		when(cache.getIfPresent(Mockito.anyString())).thenReturn(null);

		final Optional<Item> response = repository.findItemById("m2").toCompletableFuture().get();

		assertThat(response.isPresent(), is(false));

		verify(cache).getIfPresent(Mockito.anyString());
	}

	@Test
	public void findItemById_whenClientResponseWithError() throws Exception {
		when(cache.getIfPresent(Mockito.anyString()))
			.thenThrow(new IllegalArgumentException("Cache error"));

		final Optional<Item> response = repository.findItemById("m3").toCompletableFuture().get();

		assertThat(response.isPresent(), is(false));

		verify(cache).getIfPresent(Mockito.anyString());
	}

	@Test
	public void persistItem_whenClientSuccessResponse() throws Exception {
		doNothing().when(cache).put(Mockito.anyString(), Mockito.anyFloat());
		final Item response = repository.persistItem(Item.create("m1", BigDecimal.TEN))
			.toCompletableFuture().get();
		assertThat(response.getId(), is("m1"));
		assertThat(response.getPrice(), is(BigDecimal.TEN));

		verify(cache).put(Mockito.anyString(), Mockito.anyFloat());
	}

	@Test
	public void persistItem_whenClientErrorResponse() throws Exception {
		doThrow(new IllegalArgumentException("client error")).when(cache)
			.put(Mockito.anyString(), Mockito.anyFloat());

		final Throwable error = Assertions.assertThrows(Throwable.class,
			() -> repository.persistItem(Item.create("m1", BigDecimal.TEN))
				.toCompletableFuture().get());

		assertThat(error.getCause(), instanceOf(IllegalArgumentException.class));

		verify(cache).put(Mockito.anyString(), Mockito.anyFloat());
	}
}
