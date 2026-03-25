package com.n1b3lung0.supermarkets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SupermarketsApplicationTests extends PostgresIntegrationTest {

  @Test
  void contextLoads() {}
}
