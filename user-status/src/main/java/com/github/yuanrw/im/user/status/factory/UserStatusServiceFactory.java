package com.github.yuanrw.im.user.status.factory;

import com.github.yuanrw.im.user.status.service.UserStatusService;

import java.util.Properties;

/**
 * use factory pattern to create a userStatusService
 */
public interface UserStatusServiceFactory {

    /**
     * create a userStatusService
     *
     * @param properties
     * @return
     */
    UserStatusService createService(Properties properties);
}
