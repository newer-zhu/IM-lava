package com.github.yuanrw.im.rest.web.task;

import com.github.yuanrw.im.common.domain.constant.ImConstant;
import com.github.yuanrw.im.common.parse.ParseService;
import com.github.yuanrw.im.protobuf.constant.MsgTypeEnum;
import com.github.yuanrw.im.protobuf.generate.Ack;
import com.github.yuanrw.im.protobuf.generate.Chat;
import com.github.yuanrw.im.rest.web.service.OfflineService;
import com.google.protobuf.Message;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author: Hodor_Zhu
 * @description save Message from MQ to DB
 * @date: 2023/2/20 0:06
 */
@Component
public class KafkaOfflineListener {
    private Logger logger = LoggerFactory.getLogger(KafkaOfflineListener.class);

    private ParseService parseService;

    private OfflineService offlineService;

    public KafkaOfflineListener(OfflineService offlineService) {
        this.parseService = new ParseService();
        this.offlineService = offlineService;
    }

    @PostConstruct
    public void init() {
        logger.info("[KafkaOfflineListener] Start listening Offline queue......");
    }

    @KafkaListener(topics = ImConstant.KAFKA_TOPIC)
    public void onMessage(ConsumerRecord<String, Message> record){
        try {
            Message message = record.value();
            logger.info("[OfflineConsumer] getUserSpi msg: {}", message.toString());

            int code = MsgTypeEnum.getByClass(message.getClass()).getCode();
            if (code == MsgTypeEnum.CHAT.getCode()) {
                offlineService.saveChat((Chat.ChatMsg) message);
            } else {
                offlineService.saveAck((Ack.AckMsg) message);
            }

        } catch (Exception e) {
            logger.error("[OfflineConsumer] has error", e);
        } finally {

        }
    }
}
