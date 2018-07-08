package de.brokenpipe.kafkatalk.examples.simpleproducer;

import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class Controller {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void produceStuffAction() {
        IntStream.range(0, 10)
            .forEach(i -> kafkaTemplate.send(
                "test.bar",
                String.format("Key: %d", i),
                String.format("Hello from Java, msg #%d", i)
            ));
    }
}
