package testsupport;

import jakarta.persistence.EntityManager;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class EntityManagerTestConfig {

    @Bean
    public EntityManager entityManager() {
        return Mockito.mock(EntityManager.class);
    }
}