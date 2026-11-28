package com.fraudgraph.investigator.config;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

@Configuration(proxyBeanMethods = false)
class RuntimeProfiles {
    @Bean
    static BeanFactoryPostProcessor requireExclusiveRuntime(Environment environment) {
        return factory -> {
            boolean demo = environment.acceptsProfiles(Profiles.of("demo"));
            boolean imported = environment.acceptsProfiles(Profiles.of("import"));
            if (demo == imported) {
                throw new IllegalStateException("Select exactly one runtime profile: demo or import");
            }
        };
    }
}
