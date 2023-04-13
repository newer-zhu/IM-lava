package com.github.yuanrw.im.client;

import com.github.yuanrw.im.client.context.RelationCache;
import com.github.yuanrw.im.client.context.impl.MemoryRelationCache;
import com.github.yuanrw.im.client.service.ClientRestService;
import com.google.inject.AbstractModule;


public class ClientModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RelationCache.class).to(MemoryRelationCache.class);
        bind(ClientRestService.class).toProvider(ClientRestServiceProvider.class);
    }
}
