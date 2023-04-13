package com.github.yuanrw.im.protobuf.constant;

import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.github.yuanrw.im.protobuf.generate.Internal;

import java.util.stream.Stream;


public enum MsgTypeEnum {

    /**
     * chat msg
     */
    CHAT(0, Chat.ChatMsg.class),

    /**
     * app internal msg
     */
    INTERNAL(1, Internal.InternalMsg.class),

    /**
     *  app ack msg
     */
    ACK(2, Ack.AckMsg.class);

    int code;
    Class<?> clazz;

    MsgTypeEnum(int code, Class<?> clazz) {
        this.code = code;
        this.clazz = clazz;
    }

    public static MsgTypeEnum getByCode(int code) {
        return Stream.of(values()).filter(t -> t.code == code)
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    public static MsgTypeEnum getByClass(Class<?> clazz) {
        return Stream.of(values()).filter(t -> t.clazz == clazz)
            .findFirst().orElseThrow(IllegalArgumentException::new);
    }

    public int getCode() {
        return code;
    }
}
