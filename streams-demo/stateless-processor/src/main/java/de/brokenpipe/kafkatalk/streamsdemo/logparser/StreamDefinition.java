package de.brokenpipe.kafkatalk.streamsdemo.logparser;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes.StringSerde;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
@Slf4j
public class StreamDefinition {

    @Bean
    public KStream<?, ?> build(StreamsBuilder streamsBuilder) {
        KStream<String, String> stream = streamsBuilder.stream("streamsdemo.logparser.raw", Consumed.with(null, new StringSerde()));

        stream
            .peek((k, v) -> log.info("Incoming message: {}", v))
            .mapValues(LogEntrySpecification::of)
            .filter((k, v) -> v != null)
            .selectKey((k, v) -> v.getIpAddress())
            .to("streamsdemo.logparser.entries", Produced.with(new StringSerde(), new JsonSerde<>(LogEntry.class)));

        return stream;
    }
}
