package com.github.yuanrw.im.connector.service;

import com.github.yuanrw.im.common.domain.constant.MsgVersion;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.connector.domain.ClientConn;
import com.github.yuanrw.im.connector.domain.ClientConnContext;
import com.github.yuanrw.im.connector.handler.ConnectorTransferHandler;
import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.github.yuanrw.im.protobuf.generate.Internal;
import com.github.yuanrw.im.user.status.factory.UserStatusServiceFactory;
import com.github.yuanrw.im.user.status.service.UserStatusService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;
import java.util.Properties;

import static com.github.yuanrw.im.connector.start.ConnectorStarter.CONNECTOR_CONFIG;

/**
 *
 */
@Singleton
public class UserOnlineService {

    private ClientConnContext clientConnContext;
    private ConnectorToClientService connectorToClientService;
    private OfflineService offlineService;
    private UserStatusService userStatusService;

    @Inject
    public UserOnlineService(OfflineService offlineService, ClientConnContext clientConnContext,
                             ConnectorToClientService connectorToClientService, UserStatusServiceFactory userStatusServiceFactory) {
        this.clientConnContext = clientConnContext;
        this.offlineService = offlineService;
        this.connectorToClientService = connectorToClientService;

        Properties properties = new Properties();
        properties.put("host", CONNECTOR_CONFIG.getRedisHost());
        properties.put("port", CONNECTOR_CONFIG.getRedisPort());
        properties.put("password", CONNECTOR_CONFIG.getRedisPassword());
        this.userStatusService = userStatusServiceFactory.createService(properties);
    }

    public ClientConn userOnline(String userId, ChannelHandlerContext ctx) {
        //get all offline msgs and send to client
        List<Message> msgs = offlineService.pollOfflineMsg(userId);
        msgs.forEach(msg -> {
            try {
                Chat.ChatMsg chatMsg = (Chat.ChatMsg) msg;
                connectorToClientService.doChatToClientAndFlush(chatMsg);
            } catch (ClassCastException ex) {
                Ack.AckMsg ackMsg = (Ack.AckMsg) msg;
                connectorToClientService.doSendAckToClientAndFlush(ackMsg);
            }
        });

        //save connection to clientConnContext
        ClientConn conn = new ClientConn(ctx);
        conn.setUserId(userId);
        clientConnContext.addConn(conn);

        //user is online
        String oldConnectorId = userStatusService.online(userId, ConnectorTransferHandler.CONNECTOR_ID);
        if (oldConnectorId != null) {
            //can't online twice
            sendErrorToClient("user already online with the same device ID!", ctx);
        }

        return conn;
    }

    private void sendErrorToClient(String errorMsg, ChannelHandlerContext ctx) {
        Internal.InternalMsg errorAck = Internal.InternalMsg.newBuilder()
            .setId(IdWorker.snowGenId())
            .setVersion(MsgVersion.V1.getVersion())
            .setFrom(Internal.InternalMsg.Module.CONNECTOR)
            .setDest(Internal.InternalMsg.Module.CLIENT)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Internal.InternalMsg.MsgType.ERROR)
            .setMsgBody(errorMsg)
            .build();

        ctx.writeAndFlush(errorAck);
    }

    public void userOffline(ChannelHandlerContext ctx) {
        ClientConn conn = clientConnContext.getConn(ctx);
        if (conn == null) {
            return;
        }
        userStatusService.offline(conn.getUserId());
        //remove the connection
        clientConnContext.removeConn(ctx);
    }
}
