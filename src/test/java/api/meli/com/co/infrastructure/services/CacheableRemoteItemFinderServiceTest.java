package api.meli.com.co.infrastructure.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import api.meli.com.co.domain.repositories.ItemRepository;
import api.meli.com.co.domain.vo.Item;
import api.meli.com.co.infrastructure.adapters.clients.MeliItemRestClient;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

/**
 * The test cases for {@link CacheableRemoteItemFinderService}
 *
 * @author <a href="ing.josefabian@gmail.com">José Fabián Mejía</a>
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
public class CacheableRemoteItemFinderServiceTest {

	@Mock
	private MeliItemRestClient itemRestClient;

	@Mock
	private ItemRepository itemRepository;

	@InjectMocks
	private CacheableRemoteItemFinderService service;

	@Test
	public void findPricesByItemIds_whenItemsAreNotInCacheAndRemoteServiceResponseSuccess()
		throws Exception {

		when(itemRepository.findItemById(Mockito.anyString()))
			.thenReturn(CompletableFuture.completedFuture(Optional.empty()));

		when(itemRepository.persistItem(Mockito.any(Item.class)))
			.thenReturn(
				CompletableFuture.completedFuture(Item.create("m1", BigDecimal.valueOf(100))))
			.thenReturn(
				CompletableFuture.completedFuture(Item.create("m2", BigDecimal.valueOf(200))))
			.thenReturn(
				CompletableFuture.completedFuture(Item.create("m3", BigDecimal.valueOf(300))))
			.thenReturn(
				CompletableFuture.completedFuture(Item.create("m4", BigDecimal.valueOf(400))));

		when(itemRestClient.getItemPriceById(Mockito.anyString()))
			.thenReturn(Mono.just(Item.create("m1", BigDecimal.valueOf(100))))
			.thenReturn(Mono.just(Item.create("m2", BigDecimal.valueOf(200))))
			.thenReturn(Mono.just(Item.create("m3", BigDecimal.valueOf(300))))
			.thenReturn(Mono.just(Item.create("m4", BigDecimal.valueOf(400))));

		final List<Item> response = service
			.findPricesByItemIds(Arrays.asList("m1", "m2", "m3", "m4"))
			.toCompletableFuture().get();

		assertThat(response.stream().map(Item::getId).collect(Collectors.toList()),
			Matchers.containsInAnyOrder("m1", "m2", "m3", "m4"));
		assertThat(response.stream().map(Item::getPrice).collect(Collectors.toList()),
			Matchers.containsInAnyOrder(BigDecimal.valueOf(100), BigDecimal.valueOf(200),
				BigDecimal.valueOf(300), BigDecimal.valueOf(400)));

		verify(itemRepository, times(4)).findItemById(Mockito.anyString());
		verify(itemRepository, times(4)).persistItem(Mockito.any(Item.class));
		verify(itemRestClient, times(4)).getItemPriceById(Mockito.anyString());
	}

	@Test
	public void findPricesByItemIds_whenItemsAreNotInCacheAndRemoteServiceResponseWithOneError()
		throws Exception {

		when(itemRepository.findItemById(Mockito.anyString()))
			.thenReturn(CompletableFuture.completedFuture(Optional.empty()));

		when(itemRepository.persistItem(Mockito.any(Item.class)))
			.thenReturn(
				CompletableFuture.completedFuture(Item.create("m1", BigDecimal.valueOf(100))))
			.thenReturn(
				CompletableFuture.completedFuture(Item.create("m2", BigDecimal.valueOf(200))))
			.thenReturn(
				CompletableFuture.completedFuture(Item.create("m4", BigDecimal.valueOf(400))));

		when(itemRestClient.getItemPriceById(Mockito.anyString()))
			.thenReturn(Mono.just(Item.create("m1", BigDecimal.valueOf(100))))
			.thenReturn(Mono.just(Item.create("m2", BigDecimal.valueOf(200))))
			.thenReturn(Mono.error(() -> new IllegalArgumentException("Error from remote service")))
			.thenReturn(Mono.just(Item.create("m4", BigDecimal.valueOf(400))));

		final List<Item> response = service
			.findPricesByItemIds(Arrays.asList("m1", "m2", "m3", "m4"))
			.toCompletableFuture().get();

		assertThat(response.stream().map(Item::getId).collect(Collectors.toList()),
			Matchers.containsInAnyOrder("m1", "m2", "m4"));
		assertThat(response.stream().map(Item::getPrice).collect(Collectors.toList()),
			Matchers.containsInAnyOrder(BigDecimal.valueOf(100), BigDecimal.valueOf(200)
				, BigDecimal.valueOf(400)));

		verify(itemRepository, times(4)).findItemById(Mockito.anyString());
		verify(itemRepository, times(3)).persistItem(Mockito.any(Item.class));
		verify(itemRestClient, times(4)).getItemPriceById(Mockito.anyString());
	}

	@Test
	public void findPricesByItemIds_whenItemsAreNotInCacheAndRemoteServiceResponseWithAllErrors()
		throws Exception {

		when(itemRepository.findItemById(Mockito.anyString()))
			.thenReturn(CompletableFuture.completedFuture(Optional.empty()));

		when(itemRestClient.getItemPriceById(Mockito.anyString()))
			.thenReturn(Mono.error(() -> new IllegalArgumentException("Error from remote service")))
			.thenReturn(Mono.error(() -> new IllegalArgumentException("Error from remote service")))
			.thenReturn(
				Mono.error(() -> new IllegalArgumentException("Error from remote service")));

		final List<Item> response = service
			.findPricesByItemIds(Arrays.asList("m1", "m2", "m3"))
			.toCompletableFuture().get();

		assertThat(response, Matchers.empty());

		verify(itemRepository, times(3)).findItemById(Mockito.anyString());
		verifyNoMoreInteractions(itemRepository);
		verify(itemRestClient, times(3)).getItemPriceById(Mockito.anyString());
	}

	@Test
	public void findPricesByItemIds_whenSomeItemsAreInCache() throws Exception {

		when(itemRepository.findItemById(Mockito.anyString()))
			.thenReturn(CompletableFuture.completedFuture(Optional.empty()))
			.thenReturn(CompletableFuture
				.completedFuture(Optional.of(Item.create("m2", BigDecimal.valueOf(200)))))
			.thenReturn(CompletableFuture.completedFuture(Optional.empty()))
			.thenReturn(CompletableFuture
				.completedFuture(Optional.of(Item.create("m4", BigDecimal.valueOf(400)))));

		when(itemRepository.persistItem(Mockito.any(Item.class)))
			.thenReturn(
				CompletableFuture.completedFuture(Item.create("m1", BigDecimal.valueOf(100))))
			.thenReturn(
				CompletableFuture.completedFuture(Item.create("m3", BigDecimal.valueOf(300))));

		when(itemRestClient.getItemPriceById(Mockito.anyString()))
			.thenReturn(Mono.just(Item.create("m1", BigDecimal.valueOf(100))))
			.thenReturn(Mono.just(Item.create("m3", BigDecimal.valueOf(300))));

		final List<Item> response = service
			.findPricesByItemIds(Arrays.asList("m1", "m2", "m3", "m4"))
			.toCompletableFuture().get();

		assertThat(response.stream().map(Item::getId).collect(Collectors.toList()),
			Matchers.containsInAnyOrder("m1", "m2", "m3", "m4"));
		assertThat(response.stream().map(Item::getPrice).collect(Collectors.toList()),
			Matchers.containsInAnyOrder(BigDecimal.valueOf(100), BigDecimal.valueOf(200),
				BigDecimal.valueOf(300), BigDecimal.valueOf(400)));

		verify(itemRepository, times(4)).findItemById(Mockito.anyString());
		verify(itemRepository, times(2)).persistItem(Mockito.any(Item.class));
		verify(itemRestClient, times(2)).getItemPriceById(Mockito.anyString());
	}

	@Test
	public void findPricesByItemIds_whenSomeItemsAreInCacheAndRemoteServiceResponseWithOneError()
		throws Exception {

		when(itemRepository.findItemById(Mockito.anyString()))
			.thenReturn(CompletableFuture.completedFuture(Optional.empty()))
			.thenReturn(CompletableFuture
				.completedFuture(Optional.of(Item.create("m2", BigDecimal.valueOf(200)))))
			.thenReturn(CompletableFuture.completedFuture(Optional.empty()))
			.thenReturn(CompletableFuture
				.completedFuture(Optional.of(Item.create("m4", BigDecimal.valueOf(400)))));

		when(itemRepository.persistItem(Mockito.any(Item.class)))
			.thenReturn(
				CompletableFuture.completedFuture(Item.create("m3", BigDecimal.valueOf(300))));

		when(itemRestClient.getItemPriceById(Mockito.anyString()))
			.thenReturn(Mono.error(() -> new IllegalArgumentException("Error from remote service")))
			.thenReturn(Mono.just(Item.create("m3", BigDecimal.valueOf(300))));

		final List<Item> response = service
			.findPricesByItemIds(Arrays.asList("m1", "m2", "m3", "m4"))
			.toCompletableFuture().get();

		assertThat(response.stream().map(Item::getId).collect(Collectors.toList()),
			Matchers.containsInAnyOrder("m2", "m3", "m4"));
		assertThat(response.stream().map(Item::getPrice).collect(Collectors.toList()),
			Matchers.containsInAnyOrder(BigDecimal.valueOf(200), BigDecimal.valueOf(300),
				BigDecimal.valueOf(400)));

		verify(itemRepository, times(4)).findItemById(Mockito.anyString());
		verify(itemRepository).persistItem(Mockito.any(Item.class));
		verify(itemRestClient, times(2)).getItemPriceById(Mockito.anyString());
	}

	@Test
	public void findPricesByItemIds_whenAllItemsAreInCache() throws Exception {

		when(itemRepository.findItemById(Mockito.anyString()))
			.thenReturn(CompletableFuture
				.completedFuture(Optional.of(Item.create("m1", BigDecimal.valueOf(100)))))
			.thenReturn(CompletableFuture
				.completedFuture(Optional.of(Item.create("m2", BigDecimal.valueOf(200)))))
			.thenReturn(CompletableFuture
				.completedFuture(Optional.of(Item.create("m3", BigDecimal.valueOf(300)))))
			.thenReturn(CompletableFuture
				.completedFuture(Optional.of(Item.create("m4", BigDecimal.valueOf(400)))));

		final List<Item> response = service
			.findPricesByItemIds(Arrays.asList("m1", "m2", "m3", "m4"))
			.toCompletableFuture().get();

		assertThat(response.stream().map(Item::getId).collect(Collectors.toList()),
			Matchers.containsInAnyOrder("m1", "m2", "m3", "m4"));
		assertThat(response.stream().map(Item::getPrice).collect(Collectors.toList()),
			Matchers.containsInAnyOrder(BigDecimal.valueOf(100), BigDecimal.valueOf(200),
				BigDecimal.valueOf(300), BigDecimal.valueOf(400)));

		verify(itemRepository, times(4)).findItemById(Mockito.anyString());
		verifyNoMoreInteractions(itemRepository);
		verifyNoInteractions(itemRestClient);
	}

	@Test
	public void findPricesByItemIds_whenItemsAreNotInCacheTheFirstTime() throws Exception {

		when(itemRepository.findItemById(Mockito.anyString()))
			.thenReturn(CompletableFuture.completedFuture(Optional.empty()))
			.thenReturn(CompletableFuture.completedFuture(Optional.empty()))
			.thenReturn(CompletableFuture
				.completedFuture(Optional.of(Item.create("m1", BigDecimal.valueOf(100)))))
			.thenReturn(CompletableFuture
				.completedFuture(Optional.of(Item.create("m2", BigDecimal.valueOf(200)))));

		when(itemRepository.persistItem(Mockito.any(Item.class)))
			.thenReturn(
				CompletableFuture.completedFuture(Item.create("m1", BigDecimal.valueOf(100))))
			.thenReturn(
				CompletableFuture.completedFuture(Item.create("m2", BigDecimal.valueOf(200))));

		when(itemRestClient.getItemPriceById(Mockito.anyString()))
			.thenReturn(Mono.just(Item.create("m1", BigDecimal.valueOf(100))))
			.thenReturn(Mono.just(Item.create("m2", BigDecimal.valueOf(200))));

		final List<Item> response1 = service.findPricesByItemIds(Arrays.asList("m1", "m2"))
			.toCompletableFuture().get();

		assertThat(response1.stream().map(Item::getId).collect(Collectors.toList()),
			Matchers.containsInAnyOrder("m1", "m2"));
		assertThat(response1.stream().map(Item::getPrice).collect(Collectors.toList()),
			Matchers.containsInAnyOrder(BigDecimal.valueOf(100), BigDecimal.valueOf(200)));

		final List<Item> response2 = service.findPricesByItemIds(Arrays.asList("m1", "m2"))
			.toCompletableFuture().get();

		assertThat(response2.stream().map(Item::getId).collect(Collectors.toList()),
			Matchers.containsInAnyOrder("m1", "m2"));
		assertThat(response2.stream().map(Item::getPrice).collect(Collectors.toList()),
			Matchers.containsInAnyOrder(BigDecimal.valueOf(100), BigDecimal.valueOf(200)));

		verify(itemRepository, times(4)).findItemById(Mockito.anyString());
		verify(itemRepository, times(2)).persistItem(Mockito.any(Item.class));
		verify(itemRestClient, times(2)).getItemPriceById(Mockito.anyString());
	}
}