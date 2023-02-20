package com.github.yuanrw.im.common.kafka;

import com.github.yuanrw.im.common.parse.ParseService;
import com.google.protobuf.Message;
import lombok.SneakyThrows;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

/**
 * @author: Hodor_Zhu
 * @description
 * @date: 2023/2/20 20:36
 */
public class KafkaMessageDecoder implements Deserializer<Message> {
    ParseService parseService = new ParseService();

    @Override
    public void configure(Map<String, ?> map, boolean b) {

    }

    @SneakyThrows
    @Override
    public Message deserialize(String s, byte[] bytes) {
        byte[] msgBody = new byte[bytes.length - 1];
        System.arraycopy(bytes, 1, msgBody, 0, bytes.length - 1);
        Message msg = parseService.getMsgByCode(bytes[0], msgBody);
        return msg;
    }

    @Override
    public void close() {

    }
}
