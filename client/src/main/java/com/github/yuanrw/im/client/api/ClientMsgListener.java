package com.github.yuanrw.im.client.api;

import com.github.yuanrw.im.protobuf.generate.Chat;
import io.netty.channel.ChannelHandlerContext;

/**
 * 消息动作监听，用于回调
 */
public interface ClientMsgListener {

    /**
     * do when the client connect to connector successfully
     */
    void online();

    /**
     * read a msg
     *
     * @param chatMsg
     */
    void read(Chat.ChatMsg chatMsg);

    /**
     * do when a msg has been sent
     *
     * @param id chatMsg msg id
     */
    void hasSent(Long id);

    /**
     * do when a msg has been delivered
     *
     * @param id chatMsg msg id
     */
    void hasDelivered(Long id);

    /**
     * do when a msg has been read
     *
     * @param id chatMsg msg id
     */
    void hasRead(Long id);

    /**
     * do when the client disconnect to connector
     */
    void offline();

    /**
     * a exception is occurred
     *
     * @param ctx
     * @param cause
     */
    void hasException(ChannelHandlerContext ctx, Throwable cause);
}
