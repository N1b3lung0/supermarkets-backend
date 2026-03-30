package com.n1b3lung0.supermarkets.sync.infrastructure.adapter.input.scheduler;

import com.n1b3lung0.supermarkets.supermarket.application.port.output.SupermarketRepositoryPort;
import com.n1b3lung0.supermarkets.sync.application.dto.SyncSupermarketCatalogCommand;
import com.n1b3lung0.supermarkets.sync.application.port.input.command.SyncSupermarketCatalogUseCase;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Scheduled adapter that triggers a full catalog sync for every active supermarket once a day.
 *
 * <p>ShedLock ensures only one instance runs the job in multi-node deployments. The bean is only
 * registered when {@code app.scheduler.sync.enabled=true} (see {@link SchedulerConfig}).
 */
public class DailySyncScheduler {

  private static final Logger log = LoggerFactory.getLogger(DailySyncScheduler.class);

  private final SyncSupermarketCatalogUseCase syncUseCase;
  private final SupermarketRepositoryPort supermarketRepository;

  public DailySyncScheduler(
      SyncSupermarketCatalogUseCase syncUseCase, SupermarketRepositoryPort supermarketRepository) {
    this.syncUseCase = syncUseCase;
    this.supermarketRepository = supermarketRepository;
  }

  /**
   * Runs every day at 03:00 Europe/Madrid. ShedLock acquires a DB lock for at most 2 hours to
   * prevent a second instance from starting if the first is still running.
   */
  @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Madrid")
  @SchedulerLock(name = "dailySync", lockAtMostFor = "PT2H", lockAtLeastFor = "PT5M")
  public void runDailySync() {
    var supermarkets = supermarketRepository.findAllActive();
    log.info("[Scheduler] Daily sync started — {} supermarket(s) to sync", supermarkets.size());

    for (var supermarket : supermarkets) {
      try {
        log.info("[Scheduler] Syncing supermarket: {}", supermarket.getName().value());
        var runId =
            syncUseCase.execute(new SyncSupermarketCatalogCommand(supermarket.getId().value()));
        log.info(
            "[Scheduler] Sync run {} completed for supermarket {}",
            runId.value(),
            supermarket.getName().value());
      } catch (Exception ex) {
        log.error(
            "[Scheduler] Sync failed for supermarket {}: {}",
            supermarket.getName().value(),
            ex.getMessage(),
            ex);
      }
    }

    log.info("[Scheduler] Daily sync finished — {} supermarket(s) processed", supermarkets.size());
  }
}
