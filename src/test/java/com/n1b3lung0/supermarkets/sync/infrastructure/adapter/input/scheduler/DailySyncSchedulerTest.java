package com.n1b3lung0.supermarkets.sync.infrastructure.adapter.input.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.n1b3lung0.supermarkets.supermarket.application.port.output.SupermarketRepositoryPort;
import com.n1b3lung0.supermarkets.supermarket.domain.model.Supermarket;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketCountry;
import com.n1b3lung0.supermarkets.supermarket.domain.model.SupermarketName;
import com.n1b3lung0.supermarkets.sync.application.dto.SyncSupermarketCatalogCommand;
import com.n1b3lung0.supermarkets.sync.application.port.input.command.SyncSupermarketCatalogUseCase;
import com.n1b3lung0.supermarkets.sync.domain.model.SyncRunId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Step 60 — unit tests for DailySyncScheduler. */
@ExtendWith(MockitoExtension.class)
class DailySyncSchedulerTest {

  @Mock private SyncSupermarketCatalogUseCase syncUseCase;
  @Mock private SupermarketRepositoryPort supermarketRepository;

  private DailySyncScheduler scheduler() {
    return new DailySyncScheduler(syncUseCase, supermarketRepository);
  }

  @Test
  void runDailySync_callsUseCaseForEachActiveSupermarket() {
    var mercadona =
        Supermarket.create(SupermarketName.of("Mercadona"), SupermarketCountry.of("ES"));
    var lidl = Supermarket.create(SupermarketName.of("LIDL"), SupermarketCountry.of("ES"));
    given(supermarketRepository.findAllActive()).willReturn(List.of(mercadona, lidl));
    given(syncUseCase.execute(any())).willReturn(SyncRunId.generate());

    scheduler().runDailySync();

    verify(syncUseCase, times(2)).execute(any(SyncSupermarketCatalogCommand.class));
  }

  @Test
  void runDailySync_whenNoActiveSupermarkets_doesNotCallUseCase() {
    given(supermarketRepository.findAllActive()).willReturn(List.of());

    scheduler().runDailySync();

    verify(syncUseCase, never()).execute(any());
  }

  @Test
  void runDailySync_whenOneSupermarketFails_continuesWithTheRest() {
    var mercadona =
        Supermarket.create(SupermarketName.of("Mercadona"), SupermarketCountry.of("ES"));
    var lidl = Supermarket.create(SupermarketName.of("LIDL"), SupermarketCountry.of("ES"));
    given(supermarketRepository.findAllActive()).willReturn(List.of(mercadona, lidl));
    given(syncUseCase.execute(any()))
        .willThrow(new RuntimeException("scraper down"))
        .willReturn(SyncRunId.generate());

    // Should not throw — errors are caught per supermarket
    scheduler().runDailySync();

    verify(syncUseCase, times(2)).execute(any(SyncSupermarketCatalogCommand.class));
  }
}
