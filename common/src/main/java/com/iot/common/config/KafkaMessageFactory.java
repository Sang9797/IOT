package com.iot.common.config;

import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.Map;

public final class KafkaMessageFactory {

    private KafkaMessageFactory() {
    }

    public static <T> Message<T> buildMessage(String topic, String key, T payload, Map<String, Object> headers) {
        MessageBuilder<T> builder = MessageBuilder.withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.KEY, key);

        if (headers != null) {
            headers.forEach(builder::setHeader);
        }

        return builder.build();
    }
}
