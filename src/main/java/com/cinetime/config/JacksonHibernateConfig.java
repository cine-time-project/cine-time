package com.cinetime.config;



import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import com.fasterxml.jackson.databind.Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Prevents Jackson from forcing initialization of lazy JPA collections
 * (e.g., movie.getCast()) after the Hibernate session is closed.
 */
@Configuration
public class JacksonHibernateConfig {

    @Bean
    public Module hibernateModule() {
        Hibernate5JakartaModule module = new Hibernate5JakartaModule();
        // Don’t throw when encountering transient/lazy fields
        module.disable(Hibernate5JakartaModule.Feature.USE_TRANSIENT_ANNOTATION);
        // Skip uninitialized lazy collections instead of breaking serialization
        module.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING, false);
        return module;
    }
}
