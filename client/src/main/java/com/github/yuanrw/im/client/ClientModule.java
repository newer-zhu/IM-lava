package com.github.yuanrw.im.client;

import com.github.yuanrw.im.client.context.RelationCache;
import com.github.yuanrw.im.client.context.impl.MemoryRelationCache;
import com.github.yuanrw.im.client.service.ClientRestService;
import com.google.inject.AbstractModule;


public class ClientModule extends AbstractModule {

    @Override
    protected void configure() {
        //配置接口和实现类之间的依赖关系
        bind(RelationCache.class).to(MemoryRelationCache.class);
        bind(ClientRestService.class).toProvider(ClientRestServiceProvider.class);
    }
}
