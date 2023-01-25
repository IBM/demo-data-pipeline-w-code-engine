################################################################################
# © Copyright IBM Corporation 2023
#
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
################################################################################

#
# This is a sample script to invoke the event stream comsumer on the command-line.
# In this demo, the consumer takes data record from Kafka topic and persist the data records 
# to COS bucket when it reaches certain number of records or when it times out.
# 

# NOTE: Please update the settings before invoking the script.

# Project directory
PROJECT_DIR=UPDATE_TO_YOUR_PROJECT_DIR/demo-data-pipeline

# -------------------------------------------------------------------------------------
# COS related settings
# -------------------------------------------------------------------------------------
# COS endpoint, eg. https://s3.us-east.cloud-object-storage.appdomain.cloud
COS_ENDPOINT_URL=UPDATE_TO_YOUR_COS_ENDPOINT
# COS instance id, eg. it starts with crn:v1:bluemix:...
COS_SERVICE_INSTANCE_ID=UPDATE_TO_YOUR_COS_INSTANCE_ID
# COS API key
COS_API_KEY=UPDATE_TO_YOUR_COS_API_KEY
# COS location, eg. us-east
COS_LOCATION=UPDATE_TO_YOUR_COS_LOCATION
# COS object, which is name of the file uploaded to COS, eg. mytest_data.csv
COS_OBJECT_KEY=UPDATE_TO_YOUR_COS_OBJECT
# COS output bucket where the data file is saved
COS_BUCKET=UOPDATE_TO_YOUR_COS_BUCKET

# -------------------------------------------------------------------------------------
# Kafka related settings
# -------------------------------------------------------------------------------------
# Kafka producer name, eg. fsdemo-data-pipeline
KAFKA_NAME=UPDATE_TO_YOUR_PRODUCER_NAME
# Kafka bootstrap servers, it is comma-separated-list, eg. server1.cloud.ibm.com:9093,server2.cloud.ibm.com:9093:server3.cloud.ibm.com:9093
KAFKA_BOOTSTRAP_SERVERS=UPDATE_TO_YOUR_KAFKA_BOOTSTRAPE_SERVERS
# Kafka API key
KAFKA_API_KEY=UPDATE_TO_YOUR_KAFKA_API_KEY
# Kafka topic, eg. fsdemo-data-pipeline
KAFKA_TOPIC=UPDATE_TO_YOUR_KAFKA_TOPIC

# Consumer related settings
# When processing the data from the Kafka topic, the data is written to storage when it reaches the batch size or timed out.
# For testing purpose, you can set it to smaller value, eg. 10.
# Default size is 100 (set in App.java), which you can change based on your need.
BATCH_SIZE=10
DATADUMP_INTERVAL=60000

# Sample command to invoke the consumer from command-line
java -jar $PROJECT_DIR/target/demo-dataflow-1.0-SNAPSHOT-jar-with-dependencies.jar -consumer  \
--cosEndpointUrl $COS_ENDPOINT_URL --cosServiceInstanceId $COS_SERVICE_INSTANCE_ID --cosApiKey $COS_API_KEY \
--cosLocation $COS_LOCATION --cosBucketName $COS_BUCKET --cosObject $COS_OBJECT_KEY \
--kafkaName $KAFKA_NAME --kafkaBootstrapServers $KAFKA_BOOTSTRAP_SERVERS --kafkaApiKey $KAFKA_API_KEY --kafkaTopic $KAFKA_TOPIC \
--batchSize $BATCH_SIZE
