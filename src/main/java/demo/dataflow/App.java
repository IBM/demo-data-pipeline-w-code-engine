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

package demo.dataflow;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.cli.*;

import com.google.gson.JsonObject;

import demo.dataflow.consumer.ConsumerDataDumperTask;
import demo.dataflow.consumer.ConsumerDataManager;
import demo.dataflow.consumer.ConsumerRunnable;
import demo.dataflow.producer.ProducerHelper;
import demo.dataflow.util.CosDataDumper;
import demo.dataflow.util.CosHelper;
import demo.dataflow.util.IDataDumper;
import demo.dataflow.util.RelaxedParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main app to run either producer or consumer.
 * 
 * For this particular demo, producer is triggered when file is loaded to COS bucket, 
 * and producer takes the records from the file and pushes to Kafka queue.
 * 
 * Consumer is a runnable, which takes records from Kafka queue and writes to COS bucket 
 * when it gets certain number of records or when time is up.
 * 
 * Settings can be passed either via command-line parameters or via environment variables.
 * When settings are passed via environment variables, they are converted to command-line parameters,
 * and the program follows the same processing flow.
 */
public class App 
{

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static final int BATCH_SIZE = 100;
    public static final long DATADUMP_INTERVAL = 60000L;

    private static int log_strlen = 10;

    // keys to look up the environment variables
    private static String ENV_FLAG = "USE_ENV";
    private static String ENV_COS_PREFIX = "COS_PREFIX";
    private static String ENV_KAFKA_PREFIX = "KAFKA_PREFIX";
    private static String ENV_FILE_WHITE_PATTERN = "FILE_WHITE_PATTERN";
    private static String ENV_FILE_MAX_ROW = "FILE_MAX_ROW";

    // COS related environment variables
    private static String ENV_COS_LOCATION = "COS_LOCATION";
    // When CE is triggered by COS write event, the COS object is set in env var CE_SUBJECT
    private static String ENV_COS_OBJECT_KEY = "CE_SUBJECT";
    private static String ENV_COS_BUCKET = "COS_BUCKET";
    // COS endpoint url to connect
    private static String ENV_COS_ENDPOINT_URL = "COS_ENDPOINT_URL";

    // COS settings from service binding
    // This is the endpoints from COS credentials, which can help to discover the real endpoint
    private static String ENV_COS_ENDPOINTS = "_ENDPOINTS";
    private static String ENV_COS_INSTANCE_ID = "_RESOURCE_INSTANCE_ID";
    private static String ENV_COS_APIKEY = "_APIKEY";

    // Kafka related environment variables
    private static String ENV_KAFKA_NAME = "KAFKA_NAME";
    private static String ENV_KAFKA_TOPIC = "KAFKA_TOPIC";

    // Kafka settings from service binding
    private static String ENV_KAFKA_BOOTSTRAP_SERVERS = "_KAFKA_BROKERS_SASL";
    private static String ENV_KAFKA_APIKEY = "_APIKEY";


    /**
     * This is the entry point to run as a standalone program or as code engine job.
     * */
    public static void main(String[] args) {
        if (logger.isInfoEnabled())
            logger.info("yytest - entering main(String)");

        // Check whether env variables need to be used, if so, grab the settings from environment variables.
        // For example, to run this with code engine, pick up the values from env variables.
        boolean useEnv = false;
        String envFlagStr = System.getenv(ENV_FLAG);
        if (envFlagStr != null && envFlagStr.equalsIgnoreCase("true")) {

            // need to get settings from environment variables
            List<String> al = new ArrayList<String>();

            // this demo processes the data and writes to kafka topic, so set the producer flag
            al.add("--producer");

            // get general settings
            String cosPrefix = System.getenv(ENV_COS_PREFIX);
            // the prefix should not be null
            if (cosPrefix == null) return;

            String kafkaPrefix = System.getenv(ENV_KAFKA_PREFIX);
            // the prefix should not be null
            if (kafkaPrefix == null) return;

            String fileWhitePattern = System.getenv(ENV_FILE_WHITE_PATTERN);
            if (fileWhitePattern != null) {
                al.add("--fileWhitePattern");
                al.add(fileWhitePattern);
                logger.info(ENV_FILE_WHITE_PATTERN + ": " + fileWhitePattern);
            }

            String fileMaxRow = System.getenv(ENV_FILE_MAX_ROW);
            if (fileMaxRow != null) {
                al.add("--fileMaxRow");
                al.add(fileMaxRow);
                logger.info(ENV_FILE_MAX_ROW + ": " + fileMaxRow);
            }            

            String cosLocation = System.getenv(ENV_COS_LOCATION);
            if (cosLocation != null) {
                al.add("--cosLocation");
                al.add(cosLocation);
                logger.info(ENV_COS_LOCATION + ": " + cosLocation);
            }  

            String cosObjectKey = System.getenv(ENV_COS_OBJECT_KEY);
            if (cosObjectKey != null) {
                al.add("--cosObject");
                al.add(cosObjectKey);
                logger.info(ENV_COS_OBJECT_KEY + ": " + cosObjectKey);
            }  

            String cosBucket = System.getenv(ENV_COS_BUCKET);
            if (cosBucket != null) {
                al.add("--cosBucketName");
                al.add(cosBucket);
                logger.info(ENV_COS_BUCKET + ": " + cosBucket);
            }  

            String cosEndpointUrl = System.getenv(ENV_COS_ENDPOINT_URL);
            if (cosEndpointUrl != null) {
                al.add("--cosEndpointUrl");
                al.add(cosEndpointUrl);
                logger.info(cosPrefix + ENV_COS_ENDPOINT_URL + ": " + cosEndpointUrl);
            }  else {
                String cosEndpoints = System.getenv(cosPrefix + ENV_COS_ENDPOINTS);
                if (cosEndpoints != null) {
                    al.add("--cosEndpointUrl");
                    al.add(cosEndpoints);
                    logger.info(cosPrefix + ENV_COS_ENDPOINTS + ": " + cosEndpoints);
                }
            }

            String cosInstanceId = System.getenv(cosPrefix + ENV_COS_INSTANCE_ID);
            if (cosInstanceId != null) {
                al.add("--cosServiceInstanceId");
                al.add(cosInstanceId);
                logger.info(cosPrefix + ENV_COS_INSTANCE_ID + ": " + cosInstanceId);
            }  

            String cosApikey = System.getenv(cosPrefix + ENV_COS_APIKEY);
            if (cosApikey != null) {
                al.add("--cosApiKey");
                al.add(cosApikey);
                // only logs the length of the api key
                logger.info(cosPrefix + ENV_COS_APIKEY + ", len: " + cosApikey.length());
            }  

            String kafkaName = System.getenv(ENV_KAFKA_NAME);
            if (kafkaName != null) {
                al.add("--kafkaName");
                al.add(kafkaName);
                logger.info(ENV_KAFKA_NAME + ": " + kafkaName);
            }  

            String kafkaTopic = System.getenv(ENV_KAFKA_TOPIC);
            if (kafkaTopic != null) {
                al.add("--kafkaTopic");
                al.add(kafkaTopic);
                logger.info(ENV_KAFKA_TOPIC + ": " + kafkaTopic);
            }  

            String kafkaServers = System.getenv(kafkaPrefix + ENV_KAFKA_BOOTSTRAP_SERVERS);
            if (kafkaServers != null) {
                String kafkaServers2 = kafkaServers.replaceAll("[\\[\\]\"]", "");
                al.add("--kafkaBootstrapServers");
                al.add(kafkaServers2);
                logger.info(kafkaPrefix + ENV_KAFKA_BOOTSTRAP_SERVERS + "(processed): " + kafkaServers2);
            }  

            String kafkaApikey = System.getenv(kafkaPrefix + ENV_KAFKA_APIKEY);
            if (kafkaApikey != null) {
                al.add("--kafkaApiKey");
                al.add(kafkaApikey);
                // only logs the first few characters
                logger.info(kafkaPrefix + ENV_KAFKA_APIKEY + ": " + kafkaApikey.substring(0, 10) + ", len: " + kafkaApikey.length());
            }  

            // process the request
            String[] arr = new String[al.size()];
            arr = al.toArray(arr);
            process(arr);

        } else {
            process(args);
        }

    }

    /**
     * This can also run as cloud functions, which can be triggered by cloud trigger.
    // Light weight data processing can be deployed as cloud functions, otherwise, use code engine.
     * @param args commandline parameters
     * @return status
     */
    public static JsonObject main(JsonObject args) {
        if (logger.isInfoEnabled())
            logger.info("yytest - entering main(JsonObject)");
        Set<String> keySet = args.keySet();
        if (keySet.size() == 0) {
            // nothing to do, return
            return null;
        }

        // Check the environment variable, to explore the possibility to get some settings from environment variable rathen than passing in.
        // One possibility is to grant service ID access to the services needed, and get API key from env, rathern than passing in.
        // Refer to the link below for env variables
        // https://cloud.ibm.com/docs/openwhisk?topic=openwhisk-actions#actions_envvars
        String fn_name = System.getenv("__OW_ACTION_NAME");
        String fn_apikey = System.getenv("__OW_IAM_NAMESPACE_API_KEY");
        if (fn_name != null && logger.isInfoEnabled()) logger.info("fn_name: " + fn_name);
        if (fn_apikey != null && logger.isInfoEnabled()) logger.info("fn key length: " + fn_apikey.length());

        List<String> al = new ArrayList<String>();
        for (String key : keySet) {
            if (logger.isInfoEnabled())
                logger.info("Processing key: " + key);
            // Note that there are extra info being passed in besides the parameters recognized by the program, 
            // which may not be Primitive and would generate exception. Catch the exception and ignore those parameters.
            String value = null;
            try {
                value = args.getAsJsonPrimitive(key).getAsString();
                // log the first few characters for now, for debugging purpose
                if (key.toUpperCase().contains("KEY")) {
                    // do not log the value
                    if (logger.isInfoEnabled())
                        logger.info("parameter: key: " + key + ", value: secret");
                } else {
                    // logs only the first few characters to prevent sensitive info goes into log
                    if (logger.isInfoEnabled())
                        logger.info("parameter: key: " + key + ", value: " + (value.length()>log_strlen ? value.substring(0,log_strlen) : value));
                }
                
                // Cannot pass '-' or '--' as Json key, so adding it here
                String key_prefix = "-";
                if (key.length() > 2) key_prefix = "--";
                al.add(key_prefix + key);
                if (!value.equalsIgnoreCase("null")) al.add(value);
            } catch (Exception e) {
                logger.error(e.getMessage());
                continue;
            }
        }

        String[] arr = new String[al.size()];
        arr = al.toArray(arr);
        process(arr);

        // returning a dummy object for now
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("result", "success");
        return jsonObject;
    }

    private static void log(String str) {
        // print out message
        //System.out.println(str);
    }

    // Build command line options
    private static Options getOptions() {
        // parse command-line argument
        Options options = new Options();

        // producer or consumer, if absent, assume to be consumer
        Option producerFlagOption = new Option("p", "producer", false, "Producer flag");
        producerFlagOption.setRequired(false);
        options.addOption(producerFlagOption);
        Option consumerFlagOption = new Option("c", "consumer", false, "Consumer flag");
        consumerFlagOption.setRequired(false);
        options.addOption(consumerFlagOption);

        // COS parameters
        Option cosEndpointUrlOption = new Option("ce", "cosEndpointUrl", true, "COS endpoint URL");
        cosEndpointUrlOption.setRequired(true);
        options.addOption(cosEndpointUrlOption);
        Option cosServiceInstanceIdOption = new Option("ci", "cosServiceInstanceId", true, "COS service instance ID");
        cosServiceInstanceIdOption.setRequired(true);
        options.addOption(cosServiceInstanceIdOption);
        Option cosApiKeyOption = new Option("ck", "cosApiKey", true, "COS API key");
        cosApiKeyOption.setRequired(true);
        options.addOption(cosApiKeyOption);
        Option cosLocationOption = new Option("cl", "cosLocation", true, "COS location");
        cosLocationOption.setRequired(true);
        options.addOption(cosLocationOption);
        Option cosBucketNameOption = new Option("cb", "cosBucketName", true, "COS bucket name");
        cosBucketNameOption.setRequired(true);
        options.addOption(cosBucketNameOption);
        Option cosObjectOption = new Option("co", "cosObject", true, "COS object name");
        cosObjectOption.setRequired(true);
        options.addOption(cosObjectOption);

        // Kafka parameters
        Option kafkaNameOption = new Option("kn", "kafkaName", true, "Kafka name");
        kafkaNameOption.setRequired(true);
        options.addOption(kafkaNameOption);
        Option kafkaBootstrapServersOption = new Option("ks", "kafkaBootstrapServers", true, "Kafka bootstrap servers");
        kafkaBootstrapServersOption.setRequired(true);
        options.addOption(kafkaBootstrapServersOption);
        Option kafkaApiKeyOption = new Option("kk", "kafkaApiKey", true, "Kafka API key");
        kafkaApiKeyOption.setRequired(true);
        options.addOption(kafkaApiKeyOption);
        Option kafkaTopicOption = new Option("kt", "kafkaTopic", true, "Kafka topic name");
        kafkaTopicOption.setRequired(true);
        options.addOption(kafkaTopicOption);

        // extra settings
        Option fillWhitePatternOption = new Option("fp", "fileWhitePattern", true, "File name whitelist pattern");
        fillWhitePatternOption.setRequired(false);
        options.addOption(fillWhitePatternOption);        
        Option fileMaxRowOption = new Option("fr", "fileMaxRow", true, "Max rows to process from the file");
        fileMaxRowOption.setRequired(false);
        options.addOption(fileMaxRowOption);   
        Option batchSizeOption = new Option("bs", "batchSize", true, "Batch size to write to file");
        batchSizeOption.setRequired(false);
        options.addOption(batchSizeOption);  
        Option datadumpIntervalOption = new Option("di", "datadumpInterval", true, "Interval to dump data to COS in milliseconds");
        datadumpIntervalOption.setRequired(false);
        options.addOption(datadumpIntervalOption);  
        return options;
    }

    public static void process( String[] args )
    {
        if (logger.isInfoEnabled())
            logger.info( "Entering process, args str: len: " + args.length);

        Options options = getOptions();

        HelpFormatter formatter = new HelpFormatter();
        CommandLineParser parser = new RelaxedParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Command line parameters", options);
            System.exit(1);
            return;
        }

        // Only one of the producer or consumer flag should be set
        if (cmd.hasOption("p") && cmd.hasOption("c")) {
            logger.error("Either producer or consumer flag needs to be set, but not both");
            return;
        }

        boolean isProducer = false;
        if (cmd.hasOption("p")) {
            isProducer = true;
        }

        // get COS parameters
        String cosEndpointUrl = cmd.getOptionValue("cosEndpointUrl");
        String cosServiceInstanceId = cmd.getOptionValue("cosServiceInstanceId");
        String cosApiKey = cmd.getOptionValue("cosApiKey");
                                                                                                                                                                  
        //String storageClass = "us-east-standard";
        String cosLocation = cmd.getOptionValue("cosLocation"); // not an endpoint, but used in a custom function below to obtain the correct URL

        String cosBucketName = cmd.getOptionValue("cosBucketName"); // eg my-unique-bucket-name
        String cosObject = cmd.getOptionValue("cosObject");

        // get Kafka parameters
        String kafkaName = cmd.getOptionValue("kafkaName");
        String kafkaBootstrapServers = cmd.getOptionValue("kafkaBootstrapServers");
        String kafkaApiKey = cmd.getOptionValue("kafkaApiKey");
        String kafkaTopic = cmd.getOptionValue("kafkaTopic");
        
        CosHelper cosHelper = new CosHelper(cosEndpointUrl, cosServiceInstanceId, cosApiKey, cosLocation);

        if (isProducer) {
            // It is a producer. It is triggered by uploading file into COS bucket.
            // Producer reads the records from the file and pushes to Kafka queue
            String fileWhitePattern = cmd.getOptionValue("fileWhitePattern");
            fileWhitePattern = null;
            if (cmd.hasOption("fp")) {
                fileWhitePattern = cmd.getOptionValue("fileWhitePattern");
            }
    
            int fileMaxRow = -1;
            if (cmd.hasOption("fr")) {
                String fileWhitePatternStr = cmd.getOptionValue("fileMaxRow");
                fileMaxRow = Integer.parseInt(fileWhitePatternStr);
            }

            InputStreamReader inputStream = cosHelper.getObjectInputStream(cosBucketName, cosObject, fileWhitePattern, fileMaxRow);
            if (inputStream == null) return;
            ProducerHelper kafkaHelper = new ProducerHelper(kafkaName, kafkaBootstrapServers, kafkaApiKey);

            try{
                BufferedReader reader = new BufferedReader(inputStream);
                int counter = 0;
    
                String key = "key";
                while(reader.ready()) {
                    String line = reader.readLine();
                    counter++;

                    // only send max number of rows
                    if (fileMaxRow == -1 || counter <= fileMaxRow) {
                        // only log the first few characters, in case there are sensitive information
                        String log_string = line;
                        if (log_string.length() > log_strlen) log_string = line.substring(0,log_strlen);
                        logger.info("sending message: index: " + counter + ", msg: " + log_string);
                        kafkaHelper.sendMessage(kafkaTopic, key, line);
                    } else {
                        // exit
                        return;
                    }
               }
               kafkaHelper.shutdown();
            } catch (Exception ex) {
                logger.error("Caught exception: " + ex);
            }

        } else {
            // It is a consumer. Consumer takes records from Kafka queue, and writes to COS
            // when there are certain number of records or when it times out.
            int batchSize = BATCH_SIZE;
            long datadumpInterval = DATADUMP_INTERVAL;
            if (cmd.hasOption("bs")) {
                String batchSizeStr = cmd.getOptionValue("batchSize");
                batchSize = Integer.parseInt(batchSizeStr);
            }

            if (cmd.hasOption("di")) {
                String datadumpIntervalStr = cmd.getOptionValue("datadumpInterval");
                datadumpInterval = Long.parseLong(datadumpIntervalStr);
            }

            // start the consumer thread
            IDataDumper dataDumper = new CosDataDumper(cosEndpointUrl, cosServiceInstanceId, cosApiKey, cosLocation);
            ConsumerDataManager dataManager = new ConsumerDataManager(dataDumper, cosBucketName, cosObject, batchSize);

            ConsumerRunnable consumerRunnable = new ConsumerRunnable(kafkaBootstrapServers, kafkaApiKey, kafkaTopic, dataManager);
            Thread consumerThread = new Thread(consumerRunnable, "Consumer Thread");
            consumerThread.start();

            // start the timed task
            Timer timer = new Timer("ConsumerTimer");
            TimerTask task = new ConsumerDataDumperTask(dataManager);

            timer.schedule(task, datadumpInterval);
        }

    }
}
