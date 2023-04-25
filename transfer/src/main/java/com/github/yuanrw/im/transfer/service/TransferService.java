package com.github.yuanrw.im.transfer.service;

import com.github.yuanrw.im.common.domain.conn.Conn;
import com.github.yuanrw.im.common.domain.conn.ConnectorConn;
import com.github.yuanrw.im.common.domain.constant.ImConstant;
import com.github.yuanrw.im.common.domain.constant.MsgVersion;
import com.github.yuanrw.im.common.util.IdWorker;
import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.github.yuanrw.im.protobuf.generate.Internal;
import com.github.yuanrw.im.transfer.domain.ConnectorConnContext;
import com.github.yuanrw.im.transfer.start.TransferKafkaProducer;
import com.github.yuanrw.im.transfer.start.TransferStarter;
import com.google.inject.Inject;
import com.google.protobuf.Message;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

/**
 * Transfer module's specific actions to events
 */
public class TransferService {

    private ConnectorConnContext connContext;

    private TransferKafkaProducer producer;

    @Inject
    public TransferService(ConnectorConnContext connContext) {
        this.connContext = connContext;
        this.producer = TransferStarter.kafkaProducer;
    }

    /**
     * @description find dest_user's connector-connection and transfer the chat msg
     * @date 2023/4/25 15:43
     */
    public void doChat(Chat.ChatMsg msg) throws IOException {
        ConnectorConn conn = connContext.getConnByUserId(msg.getDestId());

        if (conn != null) {
            conn.getCtx().writeAndFlush(msg);
        } else {//dest user not online
            doOffline(msg);
        }
    }

    /**
     * @description find dest_user's ConnectorConn and transfer the ack msg
     * @date 2023/4/25 15:43
     */
    public void doSendAck(Ack.AckMsg msg) throws IOException {
        ConnectorConn conn = connContext.getConnByUserId(msg.getDestId());

        if (conn != null) {
            conn.getCtx().writeAndFlush(msg);
        } else {
            doOffline(msg);
        }
    }

    /**
     * set the netId from message body and save the ConnectorConn to connContext's map
     */
    public void doGreet(Internal.InternalMsg greetMsg, ChannelHandlerContext ctx) {
        //greetMsg's MsgBody is snowGenId, will be the netId as the key in connContext's map
        ctx.channel().attr(Conn.NET_ID).set(greetMsg.getMsgBody());
        ConnectorConn conn = new ConnectorConn(ctx);
        connContext.addConn(conn);

        ctx.writeAndFlush(getInternalAck(greetMsg.getId()));
    }

    private Internal.InternalMsg getInternalAck(Long msgId) {
        return Internal.InternalMsg.newBuilder()
            .setVersion(MsgVersion.V1.getVersion())
            .setId(IdWorker.snowGenId())
            .setFrom(Internal.InternalMsg.Module.TRANSFER)
            .setDest(Internal.InternalMsg.Module.CONNECTOR)
            .setCreateTime(System.currentTimeMillis())
            .setMsgType(Internal.InternalMsg.MsgType.ACK)
            .setMsgBody(msgId + "")
            .build();
    }

    /**
     * @description  send offline msg to MQ
     */
    private void doOffline(Message msg) throws IOException {
        producer.produce(ImConstant.KAFKA_TOPIC, msg);
    }
}
