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
        if (!props.containsKey(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG))
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        if (!props.containsKey(ProducerConfig.ACKS_CONFIG))
            props.put(ProducerConfig.ACKS_CONFIG, "all");
        if(!props.containsKey(ProducerConfig.RETRIES_CONFIG))
            props.put(ProducerConfig.RETRIES_CONFIG, 0);
        if (!props.containsKey(ProducerConfig.BATCH_SIZE_CONFIG))
            props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        if(!props.containsKey(ProducerConfig.LINGER_MS_CONFIG))
            props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        if(!props.containsKey(ProducerConfig.BUFFER_MEMORY_CONFIG))
            props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,  "com.github.yuanrw.im.common.kafka.KafkaMessageEncoder");

        producer = new KafkaProducer<>(props);

    }

    public void produce(String topic, Message value){
        producer.send(new ProducerRecord(topic,"", value), new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                if (exception != null) {
                    logger.error("Can't produce msg, getting error", exception);
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
