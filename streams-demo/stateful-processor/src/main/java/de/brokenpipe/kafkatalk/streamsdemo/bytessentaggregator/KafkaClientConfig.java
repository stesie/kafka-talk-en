package de.brokenpipe.kafkatalk.streamsdemo.bytessentaggregator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaClientConfig {

    @Value("${kafka.bootstrapServers:localhost:9092}")
    private String bootstrapServers;

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public StreamsConfig kStreamsConfigs() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.ByteArray().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());

        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "bytessentaggregator");
        properties.put(StreamsConfig.STATE_DIR_CONFIG, String.format("/tmp/kafka-streams/%s", UUID.randomUUID().toString()));

        return new StreamsConfig(properties);
    }

}
