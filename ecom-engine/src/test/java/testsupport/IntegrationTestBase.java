package testsupport;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for all full integration tests.
 * <pre>
 * Provides:
 *  - Full Spring context (@SpringBootTest)
 *  - MockMvc auto-configuration
 *  - IntegrationTestConfig (test security, JWT, ObjectMapper)
 *  - "test-integration" profile
 *  - @Transactional so each test runs isolated and rolls back
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(IntegrationTestConfig.class)
@ActiveProfiles("test-integration")
@Transactional
public abstract class IntegrationTestBase {
}
