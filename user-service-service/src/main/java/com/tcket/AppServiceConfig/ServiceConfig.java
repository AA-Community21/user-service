package com.tcket.AppServiceConfig;

import com.tcket.implementation.UserService;
import com.tcket.interfaces.IUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public IUserService getUserService() {
        return new UserService();
    }
}
