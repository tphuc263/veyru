package com.veyru.config;

import com.veyru.application.discovery.GraphSyncService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphReconciliationConfig {
  @Bean
  @ConditionalOnProperty(name = "graph.reconcile-on-startup", havingValue = "true")
  ApplicationRunner graphReconciliation(GraphSyncService graphSync) {
    return args -> graphSync.performFullSync();
  }
}
