/* © Copyright IBM Corporation 2023
*
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package demo.dataflow.producer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.errors.TimeoutException;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a very simple Kafka producer that sends messages in synchronized manor.
 */
public class ProducerHelper {
    private static final Logger logger = LoggerFactory.getLogger(ProducerHelper.class);

    private final String producerName;
    private final String bootstrapServers;
    private final String apikey;

    // Simple counter for messages sent
    private int producedMessages = 0;

    private KafkaProducer<String, String> kafkaProducer;
    // if run in a thread or loop, use this flag to decide whether to close the producer
    //private volatile boolean closing = false;

    public ProducerHelper(String producerName, String bootstrapServers, String apikey) {
        this.producerName = producerName;
        this.bootstrapServers = bootstrapServers;
        this.apikey = apikey;

        getProducer();
    }

    private final Map<String, Object> getProducerConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.put(ProducerConfig.CLIENT_ID_CONFIG, this.producerName);
        configs.put(ProducerConfig.ACKS_CONFIG, "all");
        configs.putAll(getCommonConfigs());
        return configs;
    }

    private final Map<String, Object> getCommonConfigs() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        configs.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        configs.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"token\" password=\"" + apikey + "\";");
        return configs;
    }

    public KafkaProducer<String, String> getProducer()
    {
        if (kafkaProducer != null) return kafkaProducer;
    
        Map<String, Object> producerConfigs = getProducerConfigs();

        // Create a Kafka producer with the provided client configuration
        kafkaProducer = new KafkaProducer<>(producerConfigs);

        return kafkaProducer;
    }

    public boolean isTopicExist(String topic) 
    {
        if (kafkaProducer == null) getProducer();
        boolean hasTopic = false;

        try {
            // Checking for topic existence.
            // If the topic does not exist, the kafkaProducer will retry for about 60 secs
            // before throwing a TimeoutException
            // see configuration parameter 'metadata.fetch.timeout.ms'
            //List<PartitionInfo> partitions = kafkaProducer.partitionsFor(topic);
            //logger.info(partitions.toString());
            hasTopic = true;
        } catch (TimeoutException kte) {
            logger.error("Topic '{}' may not exist - application will terminate", topic);
            //kafkaProducer.close(Duration.ofSeconds(5L));
            //throw new IllegalStateException("Topic '" + topic + "' may not exist - application will terminate", kte);
            // could create topic if needed, or add method to create topic
        }
        return hasTopic;
    }

    public void sendMessage(String topic, String key, String message)
    {
        if (isTopicExist(topic) == false) {
            logger.error("Topic '{}' does not exist", topic);
            return;
        }

        // check parameters
        if (message == null) {
            // nothing to send
            return;
        }

        if (key == null) {
            // if key is null, assign a key
            key = "key";
        }


        try {

                try {
                    producedMessages++;
                    // If a partition is not specified, the client will use the default partitioner to choose one.
                    ProducerRecord<String, String> record = new ProducerRecord<>(topic,key,message);

                    // Send record asynchronously
                    Future<RecordMetadata> future = kafkaProducer.send(record);

                    if (logger.isInfoEnabled())
                        logger.info("ProducerHelper: wait for response for message " + producedMessages);

                    // Synchronously wait for a response from Event Streams / Kafka on every message produced.
                    // For high throughput the future should be handled asynchronously.
                    RecordMetadata recordMetadata = future.get(5000, TimeUnit.MILLISECONDS);

                    if (logger.isInfoEnabled())
                        logger.info("Message produced count: {}, offset: {}", producedMessages, recordMetadata.offset());

                } catch (final InterruptedException e) {
                    logger.warn("Producer closing - caught exception: {}", e, e);
                } catch (final Exception e) {
                    logger.error("Sleeping for 5s - Producer has caught : {}", e, e);
                    try {
                        Thread.sleep(5000L); // Longer sleep before retrying
                    } catch (InterruptedException e1) {
                        logger.warn("Producer closing - caught exception: {}", e, e);
                    }
                }
            
        } finally {
            //kafkaProducer.close(Duration.ofSeconds(5L));
            //logger.info("{} has shut down.", ProducerHelper.class);
        }
    }

    public void shutdown() {
        //closing = true;
        logger.info("{} is shutting down.", ProducerHelper.class);
        kafkaProducer.close(Duration.ofSeconds(5L));
    }
}
