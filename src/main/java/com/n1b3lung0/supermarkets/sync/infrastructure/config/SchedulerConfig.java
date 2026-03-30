package com.n1b3lung0.supermarkets.sync.infrastructure.config;

import com.n1b3lung0.supermarkets.supermarket.application.port.output.SupermarketRepositoryPort;
import com.n1b3lung0.supermarkets.sync.application.port.input.command.SyncSupermarketCatalogUseCase;
import com.n1b3lung0.supermarkets.sync.infrastructure.adapter.input.scheduler.DailySyncScheduler;
import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring Scheduling and ShedLock.
 *
 * <p>The {@link DailySyncScheduler} bean (and the whole scheduling infrastructure) is only
 * activated when {@code app.scheduler.sync.enabled=true}, which is {@code false} in the test
 * profile to avoid unwanted background jobs during tests.
 */
@Configuration
@ConditionalOnProperty(name = "app.scheduler.sync.enabled", havingValue = "true")
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
public class SchedulerConfig {

  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    return new JdbcTemplateLockProvider(
        JdbcTemplateLockProvider.Configuration.builder()
            .withJdbcTemplate(new JdbcTemplate(dataSource))
            .usingDbTime()
            .build());
  }

  @Bean
  public DailySyncScheduler dailySyncScheduler(
      SyncSupermarketCatalogUseCase syncUseCase, SupermarketRepositoryPort supermarketRepository) {
    return new DailySyncScheduler(syncUseCase, supermarketRepository);
  }
}
