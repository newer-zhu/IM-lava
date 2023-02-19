package com.github.yuanrw.im.transfer.domain;

import com.github.yuanrw.im.common.domain.conn.ConnectorConn;
import com.github.yuanrw.im.common.domain.conn.MemoryConnContext;
import com.github.yuanrw.im.user.status.factory.UserStatusServiceFactory;
import com.github.yuanrw.im.user.status.service.UserStatusService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Properties;

import static com.github.yuanrw.im.transfer.start.TransferStarter.TRANSFER_CONFIG;

/**
 * store transfer module and connector module
 */
@Singleton
public class ConnectorConnContext extends MemoryConnContext<ConnectorConn> {

    private UserStatusService userStatusService;

    @Inject
    public ConnectorConnContext(UserStatusServiceFactory userStatusServiceFactory) {
        Properties properties = new Properties();
        properties.put("host", TRANSFER_CONFIG.getRedisHost());
        properties.put("port", TRANSFER_CONFIG.getRedisPort());
        properties.put("password", TRANSFER_CONFIG.getRedisPassword());
        this.userStatusService = userStatusServiceFactory.createService(properties);
    }

    /**
     * Get ConnectorConn By UserId
     * @param userId
     * @return ConnectorConn
     */
    public ConnectorConn getConnByUserId(String userId) {
        String connectorId = userStatusService.getConnectorId(userId);
        if (connectorId != null) {
            ConnectorConn conn = getConn(connectorId);
            if (conn != null) {
                return conn;
            } else {
                //connectorId outdated
                userStatusService.offline(userId);
            }
        }
        return null;
    }
}
