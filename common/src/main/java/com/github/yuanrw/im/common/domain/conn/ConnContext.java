package com.github.yuanrw.im.common.domain.conn;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

public interface ConnContext<C extends Conn> {

    C getConn(ChannelHandlerContext ctx);

    C getConn(Serializable netId);

    void addConn(C conn);

    void removeConn(Serializable netId);

    void removeConn(ChannelHandlerContext ctx);

    void removeAllConn();
}
