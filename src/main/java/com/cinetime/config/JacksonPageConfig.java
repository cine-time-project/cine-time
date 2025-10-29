package com.cinetime.config;


import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Fixes serialization of Page<> inside generic wrappers like ResponseMessage<Page<T>>.
 */
@Configuration
public class JacksonPageConfig {

    @Bean
    public Module springDataPageModule() {
        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(Page.class, PageImpl.class);
        return module;
    }
}
