package com.tcket.config;

import com.tcket.utils.CouchbaseUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CouchbaseConfig {

    @Bean
    public CouchbaseUtils couchbaseUtils() {
        return new CouchbaseUtils();
    }

}
