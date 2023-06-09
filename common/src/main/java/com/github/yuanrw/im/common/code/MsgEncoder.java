package com.github.yuanrw.im.common.code;

import com.github.yuanrw.im.protobuf.constant.MsgTypeEnum;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsgEncoder extends MessageToByteEncoder<Message> {
    private static final Logger logger = LoggerFactory.getLogger(MsgEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        try {
            byte[] bytes = msg.toByteArray();
            int code = MsgTypeEnum.getByClass(msg.getClass()).getCode();
            int length = bytes.length;
            //regulate message ByteBuf form
            ByteBuf buf = Unpooled.buffer(8 + length);
            buf.writeInt(length);
            buf.writeInt(code);
            buf.writeBytes(bytes);

            out.writeBytes(buf);
        } catch (Exception e) {
            logger.error("[client] msg encode has error", e);
        }
    }
}
