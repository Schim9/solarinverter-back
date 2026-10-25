package lu.kaminski.inverter.common;

import com.google.common.collect.ImmutableList;
import lombok.extern.log4j.Log4j2;
import lombok.RequiredArgsConstructor;
import lu.kaminski.inverter.config.properties.CorsProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@Log4j2
@RequiredArgsConstructor
public class AppConfig {

    private final CorsProperties corsProperties;

    @Bean
    public FilterRegistrationBean<?> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(corsProperties.getAllowedOrigins());
        config.setAllowedHeaders(ImmutableList.of("*"));
        config.setAllowedMethods(ImmutableList.of("*"));
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<?> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
