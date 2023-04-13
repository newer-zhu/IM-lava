package com.github.yuanrw.im.common.domain.conn;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

import java.io.Serializable;


public interface Conn {

    AttributeKey<Serializable> NET_ID = AttributeKey.valueOf("netId");

    Serializable getNetId();

    ChannelHandlerContext getCtx();

    ChannelFuture close();
}
