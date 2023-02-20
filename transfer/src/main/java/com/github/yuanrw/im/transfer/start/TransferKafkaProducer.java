package com.github.yuanrw.im.transfer.start;

import com.google.inject.Singleton;
import com.google.protobuf.Message;
import io.netty.handler.codec.serialization.ObjectEncoder;
import org.apache.kafka.clients.producer.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author: Hodor_Zhu
 * @description Kafka Producer With CallBack
 * @date: 2023/2/19 23:49
 */
@Singleton
public class TransferKafkaProducer {

    private static Logger logger = LoggerFactory.getLogger(TransferKafkaProducer.class);

    private KafkaProducer<String, String> producer;

    public TransferKafkaProducer(Properties props) {
        // Kafka集群   服务端的主机名和端口号
        if (!props.containsKey(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG))
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // 等待所有副本节点的应答
        if (!props.containsKey(ProducerConfig.ACKS_CONFIG))
            props.put(ProducerConfig.ACKS_CONFIG, "all");
        // 消息发送最大尝试次数
        if(!props.containsKey(ProducerConfig.RETRIES_CONFIG))
            props.put(ProducerConfig.RETRIES_CONFIG, 0);
        // 一批消息处理大小
        if (!props.containsKey(ProducerConfig.BATCH_SIZE_CONFIG))
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        // 请求延时
        if(!props.containsKey(ProducerConfig.LINGER_MS_CONFIG))
            props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        // 发送缓存区内存大小
        if(!props.containsKey(ProducerConfig.BUFFER_MEMORY_CONFIG))
            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        // key序列化
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        // value序列化
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,  "com.github.yuanrw.im.common.kafka.KafkaMessageEncoder");

        producer = new KafkaProducer<>(props);

    }

    public void produce(String topic, Message value){
        producer.send(new ProducerRecord(topic, value), new Callback() {

            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (metadata != null) {
                    logger.error("[kafka producer] metadata.partition="+metadata.partition()
                            + "---metadata.offset=" + metadata.offset());
                }
            }
        });
    }

    public Producer getProducer() {
        return producer;
    }

    public void close(){
        producer.close();
    }
}
