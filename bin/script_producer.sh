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
# In the batch data pipeline demo, when Code Engine job is used to process the batch data, 
# the producer code engine job is triggered when a file is uploaded into COS bucket, 
# and the the job reads in settings from environment variables.
#
# To test the producer manually, you can manually upload a file into COS bucket.
# The producer can either pick up settings from command-line parameters or from 
# environment variables (similar to what code engine job does).
#
# This sample script invokes the producer on the command-line by passing settings via command-line parameters.
# 

# NOTE: Please update the settings before invoking the script.

# Project directory
PROJECT_DIR=UPDATE_TO_YOUR_PROJECT_DIR

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
# COS input bucket where the data file is uploaded
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

# -------------------------------------------------------------------------------------
# Producer related settings
# -------------------------------------------------------------------------------------
# COS object name prefix. When producer is triggered, it will ignore the upload if it is not the recognized pattern.
FILE_WHITE_PATTERN=test_data
# Maximum rows to process from the data file. For testing purpose, this can be set to a smaller value, eg. 10.
# This is optional. Remove it to process the whole file.
FILE_MAX_ROW=10

# Sample command to invoke the producer from command-line
java -jar $PROJECT_DIR/target/demo-dataflow-1.0-SNAPSHOT-jar-with-dependencies.jar --producer  \
--cosEndpointUrl $COS_ENDPOINT_URL --cosServiceInstanceId $COS_SERVICE_INSTANCE_ID --cosApiKey $COS_API_KEY \
--cosLocation $COS_LOCATION --cosBucketName $COS_BUCKET --cosObject $COS_OBJECT_KEY \
--kafkaName $KAFKA_NAME --kafkaBootstrapServers $KAFKA_BOOTSTRAP_SERVERS --kafkaApiKey $KAFKA_API_KEY --kafkaTopic $KAFKA_TOPIC \
--fileWhitePattern $FILE_WHITE_PATTERN --fileMaxRow $FILE_MAX_ROW


