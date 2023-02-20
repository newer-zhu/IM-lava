package com.github.yuanrw.im.common.kafka;

import com.github.yuanrw.im.protobuf.constant.MsgTypeEnum;
import com.google.protobuf.Message;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

/**
 * @author: Hodor_Zhu
 * @description
 * @date: 2023/2/20 20:30
 */
public class KafkaMessageEncoder implements Serializer<Message> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @SneakyThrows
    @Override
    public byte[] serialize(String s, Message message) {
        int code = MsgTypeEnum.getByClass(message.getClass()).getCode();

        byte[] destB = new byte[message.toByteArray().length + 1];
        destB[0] = (byte) code;

        System.arraycopy(message.toByteArray(), 0, destB, 1, message.toByteArray().length);
        return destB;
    }

    @Override
    public void close() {

    }
}
