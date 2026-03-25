package com.n1b3lung0.supermarkets.shared.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * ArchUnit architecture rules enforcing the hexagonal + DDD layer boundaries. These tests run on
 * every CI pipeline execution and must always pass.
 */
@AnalyzeClasses(packages = "com.n1b3lung0.supermarkets")
class ArchitectureTest {

  /**
   * The domain layer must be a pure Java island: no Spring, no JPA, no application or
   * infrastructure dependencies whatsoever.
   */
  @ArchTest
  static final ArchRule domainIsIsolated =
      noClasses()
          .that()
          .resideInAPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "org.springframework..",
              "jakarta.persistence..",
              "..application..",
              "..infrastructure..")
          .allowEmptyShould(true);

  /**
   * Application layer (use cases + handlers) must not import anything from infrastructure.
   * Infrastructure adapts to the application, never the other way around.
   */
  @ArchTest
  static final ArchRule applicationDoesNotDependOnInfrastructure =
      noClasses()
          .that()
          .resideInAPackage("..application..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("..infrastructure..")
          .allowEmptyShould(true);

  /**
   * Spring stereotype and transactional annotations are forbidden in domain and application layers.
   * They belong exclusively in infrastructure.
   */
  @ArchTest
  static final ArchRule noSpringAnnotationsInDomainOrApplication =
      noClasses()
          .that()
          .resideInAnyPackage("..domain..", "..application..")
          .should()
          .beAnnotatedWith("org.springframework.stereotype.Service")
          .orShould()
          .beAnnotatedWith("org.springframework.stereotype.Component")
          .orShould()
          .beAnnotatedWith("org.springframework.stereotype.Repository")
          .orShould()
          .beAnnotatedWith("org.springframework.transaction.annotation.Transactional")
          .allowEmptyShould(true);
}
