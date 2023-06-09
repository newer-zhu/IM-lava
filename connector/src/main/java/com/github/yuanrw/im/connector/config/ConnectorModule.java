package com.github.yuanrw.im.connector.config;

import com.github.yuanrw.im.user.status.factory.UserStatusServiceFactory;
import com.github.yuanrw.im.user.status.service.UserStatusService;
import com.github.yuanrw.im.user.status.service.impl.RedisUserStatusServiceImpl;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ConnectorModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
            .implement(UserStatusService.class, RedisUserStatusServiceImpl.class)
            .build(UserStatusServiceFactory.class));
        install(new FactoryModuleBuilder().build(ConnectorRestServiceFactory.class));
    }
}
