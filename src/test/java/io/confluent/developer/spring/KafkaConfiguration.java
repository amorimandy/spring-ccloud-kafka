//package io.confluent.developer.spring;
//
//import org.apache.kafka.clients.admin.AdminClientConfig;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.core.*;
//
//import java.util.Map;
//
//import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
//import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
//import static org.apache.kafka.clients.producer.ProducerConfig.*;
//
//@Configuration
//@EnableKafka
//public class KafkaConfiguration {
//
//    @Bean
//    public static ProducerFactory<String, String> producerFactory() {
//        return new DefaultKafkaProducerFactory<>(
//                Map.of(BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
//                        RETRIES_CONFIG, 0,
//                        BUFFER_MEMORY_CONFIG, 33554432,
//                        KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
//                        VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
//                ));
//    }
//
//    @Bean
//    public KafkaTemplate<String, String> kafkaTemplate() {
//        return new KafkaTemplate<>(producerFactory());
//    }
//
//}
//
//
//    @Bean
//    public ConsumerFactory<String, String> consumerFactory() {
//        return new DefaultKafkaConsumerFactory<>(consumerProperties());
//    }
//
//
//    @Bean
//    public KafkaAdmin admin() {
//        return new KafkaAdmin(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"));
//    }
//
//
//    @Bean
//    @KafkaListener
//    public static Map<String, Object> consumerProperties() {
//        return Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092",
//                GROUP_ID_CONFIG, "spring-ccloud",
//                ENABLE_AUTO_COMMIT_CONFIG, false,
//                SESSION_TIMEOUT_MS_CONFIG, 15000,
//                KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
//                VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//    }
//
//    @Bean
//    public NewTopic topic1() {
//        return TopicBuilder.name("thing1")
//                .partitions(10)
//                .replicas(3)
//                .compact()
//                .build();
//    }
//
//    @Bean
//    public NewTopic topic2() {
//        return TopicBuilder.name("thing2")
//                .partitions(10)
//                .replicas(3)
//                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
//                .build();
//    }
//
//    @Bean
//    public NewTopic topic3() {
//        return TopicBuilder.name("thing3")
//                .assignReplicas(0, Arrays.asList(0,1))
//                .assignReplicas(1, Arrays.asList(1,2))
//                .assignReplicas(2, Arrays.asList(2,0))
//                .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
//                .build();
//    }
//
//}
