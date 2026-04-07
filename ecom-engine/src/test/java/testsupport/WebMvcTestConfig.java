package testsupport;

import com.swirl.ecomengine.security.user.AuthenticatedUserArgumentResolver;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@TestConfiguration
public class WebMvcTestConfig implements WebMvcConfigurer {

    private final AuthenticatedUserArgumentResolver resolver;

    public WebMvcTestConfig(AuthenticatedUserArgumentResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(resolver);
    }
}
