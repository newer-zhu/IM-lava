package com.github.yuanrw.im.client.api;

import com.github.yuanrw.im.client.context.UserContext;
import com.github.yuanrw.im.client.domain.Friend;
import com.github.yuanrw.im.client.handler.ClientConnectorHandler;
import com.github.yuanrw.im.client.service.ClientRestService;
import com.github.yuanrw.im.common.domain.UserInfo;
import com.github.yuanrw.im.common.domain.constant.MsgVersion;
import com.github.yuanrw.im.common.domain.po.RelationDetail;
import com.github.yuanrw.im.common.exception.ImException;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.protobuf.generate.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * UserApi
 */
public class UserApi {
    private Logger logger = LoggerFactory.getLogger(UserApi.class);

    private ClientRestService clientRestService;
    private UserContext userContext;
    private ClientConnectorHandler handler;

    public UserApi(ClientRestService clientRestService, UserContext userContext, ClientConnectorHandler handler) {
        this.clientRestService = clientRestService;
        this.userContext = userContext;
        this.handler = handler;
    }

    private static List<Friend> getFriends(List<RelationDetail> relations, String userId) {
        return relations.stream().map(r -> {
            Friend friend = new Friend();
            if (r.getUserId1().equals(userId)) {
                friend.setUserId(r.getUserId2());
                friend.setUsername(r.getUsername2());
            } else {
                friend.setUserId(r.getUserId1());
                friend.setUsername(r.getUsername1());
            }
            friend.setEncryptKey(r.getEncryptKey());
            return friend;
        }).collect(Collectors.toList());
    }

    /**
     * login
     * @param username
     * @param password
     * @return
     */
    public UserInfo login(String username, String password) {
        UserInfo userInfo = clientRestService.login(username, password);
        //wait ack from connector
        greetToConnector(userInfo.getId());

        assert userInfo.getId() != null;

        userContext.setUserId(userInfo.getId());
        userContext.setToken(userInfo.getToken());
        userContext.addRelations(userInfo.getRelations());
        return userInfo;
    }


    private void greetToConnector(String userId) {
        Internal.InternalMsg greet = Internal.InternalMsg.newBuilder()
            .setId(IdWorker.snowGenId())
            .setFrom(Internal.InternalMsg.Module.CLIENT)
            .setDest(Internal.InternalMsg.Module.CONNECTOR)
            .setCreateTime(System.currentTimeMillis())
            .setVersion(MsgVersion.V1.getVersion())
            .setMsgType(Internal.InternalMsg.MsgType.GREET)
            .setMsgBody(userId)
            .build();

        try {
            handler.getServerAckWindow().offer(greet.getId(), greet,
                m -> handler.getCtx().writeAndFlush(m))
                .get(3, TimeUnit.SECONDS);
            logger.info("[client] client connect to server successfully");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ImException("[client] waiting for connector's response failed", e);
        }
    }

    public Void logout(String token) {
        return clientRestService.logout(token);
    }

    public List<Friend> friends(String token) {
        return getFriends(clientRestService.friends(userContext.getUserId(), token), userContext.getUserId());
    }
}
