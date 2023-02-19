package com.github.yuanrw.im.client;

import com.github.yuanrw.im.client.service.ClientRestService;
import com.google.inject.Provider;

public class ClientRestServiceProvider implements Provider<ClientRestService> {

    public static String REST_URL;

    @Override
    public ClientRestService get() {
        return new ClientRestService(REST_URL);
    }
}
