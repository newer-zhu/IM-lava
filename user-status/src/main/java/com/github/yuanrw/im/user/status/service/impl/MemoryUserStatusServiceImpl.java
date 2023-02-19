package com.github.yuanrw.im.user.status.service.impl;

import com.github.yuanrw.im.user.status.service.UserStatusService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * manage user status in jvm
 */
public class MemoryUserStatusServiceImpl implements UserStatusService {

    //userId -> connectorId
    private ConcurrentMap<String, String> userIdConnectorIdMap;

    public MemoryUserStatusServiceImpl() {
        this.userIdConnectorIdMap = new ConcurrentHashMap<>();
    }

    @Override
    public String online(String userId, String connectorId) {
        return userIdConnectorIdMap.put(userId, connectorId);
    }

    @Override
    public void offline(String userId) {
        userIdConnectorIdMap.remove(userId);
    }

    @Override
    public String getConnectorId(String userId) {
        return userIdConnectorIdMap.get(userId);
    }
}
