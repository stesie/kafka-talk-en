package de.brokenpipe.kafkatalk.streamsdemo.bytessentaggregator;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes.LongSerde;
import org.apache.kafka.common.serialization.Serdes.StringSerde;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Serialized;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
@Slf4j
public class StreamDefinition {

    @Bean
    public KStream<?, ?> build(StreamsBuilder streamsBuilder) {
        KStream<String, LogEntry> stream = streamsBuilder.stream("streamsdemo.logparser.entries",
            Consumed.with(new StringSerde(), new JsonSerde<>(LogEntry.class)));

        stream
            .peek((k, v) -> log.info("Incoming message: {}/{}", k, v))
            .mapValues(LogEntry::getBytesSent)
            .groupByKey(Serialized.with(new StringSerde(), new LongSerde()))
            .reduce((a, b) -> a + b)
            .toStream()
            .foreach((k, v) -> log.info("Bytes sent to {} so far: {}", k, v));

        return stream;
    }
}
