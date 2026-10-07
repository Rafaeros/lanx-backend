package br.lanx.api.auth.authorization.adapter.in.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class PermissionWebConfig implements WebMvcConfigurer {

    private final PermissionInterceptor interceptor;

    PermissionWebConfig(PermissionInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }
}
