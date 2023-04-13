package com.github.yuanrw.im.connector.config;

import com.github.yuanrw.im.connector.service.rest.ConnectorRestService;


public interface ConnectorRestServiceFactory {

    /**
     * todo: need to be singleton
     */
    ConnectorRestService createService(String url);
}
