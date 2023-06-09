package com.github.yuanrw.im.client.handler;

import com.github.yuanrw.im.client.api.ClientMsgListener;
import com.github.yuanrw.im.common.domain.ack.ClientAckWindow;
import com.github.yuanrw.im.common.domain.ack.ServerAckWindow;
import com.github.yuanrw.im.common.domain.constant.ImConstant;
import com.github.yuanrw.im.common.parse.AbstractMsgParser;
import com.github.yuanrw.im.common.parse.AckParser;
import com.github.yuanrw.im.common.parse.InternalParser;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.github.yuanrw.im.protobuf.generate.Internal;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Duration;
import java.util.function.Consumer;

import static com.github.yuanrw.im.common.parse.AbstractMsgParser.checkDest;
import static com.github.yuanrw.im.common.parse.AbstractMsgParser.checkFrom;

public class ClientConnectorHandler extends SimpleChannelInboundHandler<Message> {
    private Logger logger = LoggerFactory.getLogger(ClientConnectorHandler.class);

    private ClientMsgListener clientMsgListener;
    private FromConnectorParser fromConnectorParser;
    private ChannelHandlerContext ctx;

    private ServerAckWindow serverAckWindow;
    private ClientAckWindow clientAckWindow;

    private String connectionId;
    private String deviceId;

    public ClientConnectorHandler(ClientMsgListener clientMsgListener, String connectionId, String deviceId) {
        assert clientMsgListener != null;

        this.clientMsgListener = clientMsgListener;
        this.connectionId = connectionId;
        this.deviceId = deviceId;
        this.fromConnectorParser = new FromConnectorParser();
    }

    /**
     * @author hodor_zhu
     * @description execute when established a connection to connector
     * @date 2022/11/1 11:58
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().attr(AttributeKey.newInstance(ImConstant.DEVICE_ID)).set(this.deviceId);
        this.ctx = ctx;
        serverAckWindow = new ServerAckWindow(connectionId, 10, Duration.ofSeconds(5));
        clientAckWindow = new ClientAckWindow(5);
        clientMsgListener.online();
    }

    /**
     * @author hodor_zhu
     * @description msg from connector
     * @date 2022/11/1 11:58
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        logger.debug("[client] get msg: {}", msg.toString());

        //check protobuf msg's form
        checkFrom(msg, Internal.InternalMsg.Module.CONNECTOR);
        checkDest(msg, Internal.InternalMsg.Module.CLIENT);

        fromConnectorParser.parse(msg, ctx);
    }

    class FromConnectorParser extends AbstractMsgParser {
        @Override
        public void registerParsers() {
            InternalParser internalParser = new InternalParser(3);
            internalParser.register(Internal.InternalMsg.MsgType.ACK, (m, ctx) ->
                    serverAckWindow.ack(m));
            internalParser.register(Internal.InternalMsg.MsgType.ERROR, (m, ctx) ->
                    logger.error("[client] get error from connector: {}", m.getMsgBody()));

            AckParser ackParser = new AckParser(2);
            ackParser.register(Ack.AckMsg.MsgType.DELIVERED, (m, ctx) ->
                    offerAck(m.getId(), m, ignore -> clientMsgListener.hasDelivered(m.getAckMsgId())));
            ackParser.register(Ack.AckMsg.MsgType.READ, (m, ctx) ->
                    offerAck(m.getId(), m, ignore -> clientMsgListener.hasRead(m.getAckMsgId())));

            register(Chat.ChatMsg.class, (m, ctx) ->
                    offerChat(m.getId(), m, ignore -> clientMsgListener.read(m)));
            register(Ack.AckMsg.class, ackParser.generateFun());
            register(Internal.InternalMsg.class, internalParser.generateFun());
        }

        private void offerChat(Long id, Chat.ChatMsg m, Consumer<Message> consumer) {
            Chat.ChatMsg copy = Chat.ChatMsg.newBuilder().mergeFrom(m).build();
            offer(id, copy, consumer);
        }

        private void offerAck(Long id, Ack.AckMsg m, Consumer<Message> consumer) {
            Ack.AckMsg copy = Ack.AckMsg.newBuilder().mergeFrom(m).build();
            offer(id, copy, consumer);
        }

        private void offer(Long id, Message m, Consumer<Message> consumer) {
            clientAckWindow.offer(id,
                    Internal.InternalMsg.Module.CLIENT,
                    Internal.InternalMsg.Module.CONNECTOR,
                    ctx, m, consumer);
        }
    }

    /**
     * @author hodor_zhu
     * @description use ServerAckWindow, send msg to connector
     * @date 2023/4/12 23:41
     */
    public void writeAndFlush(Serializable connectionId, Long msgId, Message message) {
        ServerAckWindow.offer(connectionId, msgId, message, m -> ctx.writeAndFlush(m))
                .thenAccept(m -> clientMsgListener.hasSent(msgId))
                .exceptionally(e -> {
                    logger.error("[client] send to connector failed", e);
                    return null;
                });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug("[client] disconnect to connector");
        clientMsgListener.offline();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("[client] has error: ", cause);
        clientMsgListener.hasException(ctx, cause);
    }


    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public ServerAckWindow getServerAckWindow() {
        return serverAckWindow;
    }

    /**
     * do not use, for test
     *
     * @param serverAckWindow
     */
    public void setServerAckWindow(ServerAckWindow serverAckWindow) {
        this.serverAckWindow = serverAckWindow;
    }

    /**
     * do not use, for test
     *
     * @param clientAckWindow
     */
    public void setClientAckWindow(ClientAckWindow clientAckWindow) {
        this.clientAckWindow = clientAckWindow;
    }

    /**
     * do not use, for test
     *
     * @param ctx
     */
    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }
}
