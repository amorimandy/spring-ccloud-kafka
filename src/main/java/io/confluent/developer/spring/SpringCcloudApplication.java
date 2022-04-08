package io.confluent.developer.spring;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.apache.kafka.streams.state.QueryableStoreTypes.keyValueStore;

@SpringBootApplication
@EnableKafkaStreams
public class SpringCcloudApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCcloudApplication.class, args);
    }

    @Bean
    NewTopic hobbit2() {
        return TopicBuilder.name("hobbit2").partitions(15).replicas(3).build();
    }

    @Bean
    NewTopic count() {
        return TopicBuilder.name("streams-wordcount-output").partitions(6).replicas(3).build();
    }
}

@RequiredArgsConstructor
@Component
class Producer {

    private final KafkaTemplate<Integer, String> template;

    Faker faker;

    @EventListener(ApplicationStartedEvent.class)
    public void generate() {

        faker = Faker.instance();

        final Flux<Long> interval = Flux.interval(Duration.ofMillis(10_000));

        final Flux<String> quotes = Flux.fromStream(Stream.generate(() -> faker.hobbit().quote()));

        Flux.zip(interval, quotes)
                .map(objects -> template.send("hobbit", faker.random().nextInt(42),
                        objects.getT2())).blockLast();
    }

}

@Component
class Consumer {

    @KafkaListener(topics = {"streams-wordcount-output"}, groupId = "spring-boot-kafka")
    public void consume(ConsumerRecord<String, Long> record) {
        System.out.println("received = " + record.value() + " with key " + record.key());
    }
}

@Component
class Processor {

    @Autowired
    public void process(StreamsBuilder builder) {

        //Serializers/deserializers (serde) for String and Long types
        final Serde<Integer> integerSerde = Serdes.Integer();
        final Serde<String> stringSerde = Serdes.String();
        final Serde<Long> longSerde = Serdes.Long();

        // Construct a KStream from the input topic "streams-plaintext-input", where message values represente lines
        // of text (for the sake of this example, we ignore whatever may be stores in the message keys).
        KStream<Integer, String> textLines = builder.stream("hobbit", Consumed.with
                (integerSerde, stringSerde));

        KTable<String, Long> wordCounts = textLines
                // Split each text line, by whitespace, into words. The text lines are the message values, i.e. we can
                // ignore whatever data is in the message keys and thus invoke 'flatMapValues' instead of the more
                // generic 'flatMap'
                .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
                //We use 'groupBy' to ensure the words are available as message keys
                .groupBy((key, value) -> value, Grouped.with(stringSerde, stringSerde))
                // Count the occurrences of each word (message key).
                .count(Materialized.as("counts"));

        //Convert the 'KTable<String, Long>' into a 'KStream<String, Long>' and write to the output topic.
        wordCounts.toStream().to("streams-wordcount-output", Produced.with(stringSerde, longSerde));

    }
}

@RestController
@RequiredArgsConstructor
class RestService {

    private final StreamsBuilderFactoryBean factoryBean;

    @GetMapping("/count/{word}")
    public Long getCount(@PathVariable String word) {
        final KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();

        final ReadOnlyKeyValueStore<String, Long> counts = kafkaStreams.store(StoreQueryParameters
                .fromNameAndType("counts", keyValueStore()));
        return counts.get(word);
    }
}
