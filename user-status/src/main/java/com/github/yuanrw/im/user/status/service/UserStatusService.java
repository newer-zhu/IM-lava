package com.github.yuanrw.im.user.status.service;

/**
 * user status service
 */
public interface UserStatusService {

    /**
     * user online
     *
     * @param userId
     * @param connectorId
     * @return the user's previous connection id, if don't exist then return null
     */
    String online(String userId, String connectorId);

    /**
     * user offline
     *
     * @param userId
     */
    void offline(String userId);

    /**
     * get connector id by user id
     *
     * @param userId
     * @return
     */
    String getConnectorId(String userId);

    /**
     * whether user online or not
     * @return
     */
    Boolean isOnline(String userId);
}
