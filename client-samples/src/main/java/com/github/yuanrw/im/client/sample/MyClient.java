package com.github.yuanrw.im.client.sample;

import com.github.yuanrw.im.client.ImClient;
import com.github.yuanrw.im.client.api.ChatApi;
import com.github.yuanrw.im.client.api.ClientMsgListener;
import com.github.yuanrw.im.client.api.UserApi;
import com.github.yuanrw.im.client.domain.Friend;
import com.github.yuanrw.im.common.domain.UserInfo;
import com.github.yuanrw.im.protobuf.generate.Chat;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Date: 2019-07-09
 * Time: 08:08
 *
 * @author yrw
 */
public class MyClient {
    private static Logger logger = LoggerFactory.getLogger(MyClient.class);

    private ChatApi chatApi;
    private UserInfo userInfo;

    private Map<String, Friend> friendMap;

    public MyClient(String connectorHost, Integer connectorPort, String restUrl, String username, String password) {
        ImClient imClient = start(connectorHost, connectorPort, restUrl);
        chatApi = imClient.chatApi();
        UserApi userApi = imClient.userApi();

        //login and get a token
        userInfo = userApi.login(username, password);
        //get friends list
        List<Friend> friends = userApi.friends(userInfo.getToken());
        friendMap = friends.stream().collect(Collectors.toMap(Friend::getUserId, f -> f));

        System.out.println("Here are my friends!");
        for (Friend friend : friends) {
            System.out.println(friend.getUserId() + ": " + friend.getUsername());
        }
        System.out.println("===============================");
    }

    /**
     * init and start
     * @param connectorHost
     * @param connectorPort
     * @param restUrl
     * @return
     */
    private ImClient start(String connectorHost, Integer connectorPort, String restUrl) {
        ImClient imClient = new ImClient(connectorHost, connectorPort, restUrl, "deviceId");
        imClient.setClientMsgListener(new ClientMsgListener() {
            @Override
            public void online() {
                logger.info("[client] i have connected to server!");
                System.out.println("========online=========");
            }

            @Override
            public void read(Chat.ChatMsg chatMsg) {
                System.out.println(friendMap.get(chatMsg.getFromId()).getUsername() + ": "
                    + chatMsg.getMsgBody().toStringUtf8());
                chatApi.confirmRead(chatMsg);
                System.out.println("========read=========");
            }

            @Override
            public void hasSent(Long id) {
                System.out.println(String.format("msg {%d} has been sent", id));
                System.out.println("========hasSent=========");
            }

            @Override
            public void hasDelivered(Long id) {
                System.out.println(String.format("msg {%d} has been delivered", id));
                System.out.println("========hasDelivered=========");
            }

            @Override
            public void hasRead(Long id) {
                System.out.println(String.format("msg {%d} has been read", id));
                System.out.println("========hasRead=========");
            }

            @Override
            public void offline() {
                logger.info("[{}] I am offline!", userInfo != null ? userInfo.getUsername() : "client");
                System.out.println("========offline=========");
            }

            @Override
            public void hasException(ChannelHandlerContext ctx, Throwable cause) {
                logger.error("[" + userInfo.getUsername() + "] has error ", cause);
                System.out.println("========hasException=========");
            }
        });

        imClient.start();

        return imClient;
    }

    public void printUserInfo() {
        System.out.println("id: " + userInfo.getId());
        System.out.println("username: " + userInfo.getUsername());
    }

    public void send(String id, String text) {
        if (!friendMap.containsKey(id)) {
            System.out.println("friend " + id + " not found!");
            return;
        }
        chatApi.text(id, text);
    }
}
