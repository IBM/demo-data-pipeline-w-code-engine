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
# This sample script sets environment variables and invokes the producer on the command-line.
# 

# Update the settings before invoking the script

# Project directory
PROJECT_DIR=UPDATE_TO_YOUR_PROJECT_DIR

# Flag to indicate to pick up settings from environment varialbes
export USE_ENV=true
# This is the prefix for the CE service binding
export COS_PREFIX=COS
# This is the prefix for the CE service binding
export KAFKA_PREFIX=KAFKA

# -------------------------------------------------------------------------------------
# Producer related settings
# -------------------------------------------------------------------------------------
# COS object prefix. When producer is triggered, it will ignore the upload if it is not the recognized pattern.
export FILE_WHITE_PATTERN=test_data
# Maximum rows to process from the data file. For testing purpose, this can be set to a smaller value, eg. 10.
# This is optional. Remove it to process the whole file.
export FILE_MAX_ROW=10

# -------------------------------------------------------------------------------------
# COS related settings
# -------------------------------------------------------------------------------------
# COS location, eg. us-east
export COS_LOCATION=UPDATE_TO_YOUR_COS_LOCATION
# COS object, which is name of the file uploaded to COS, eg. mytest_data.csv
export CE_SUBJECT=UPDATE_TO_YOUR_COS_OBJECT
# COS input bucket where the data file is uploaded
export COS_BUCKET=UOPDATE_TO_YOUR_COS_BUCKET

# The COS endpoint to connect
# COS endpoint, eg. https://s3.us-east.cloud-object-storage.appdomain.cloud
export COS_ENDPOINT_URL=UPDATE_TO_YOUR_COS_ENDPOINT
# COS instance id, eg. it starts with crn:v1:bluemix:...
export COS_RESOURCE_INSTANCE_ID=UPDATE_TO_YOUR_COS_INSTANCE_ID
# COS API key
export COS_APIKEY=UPDATE_TO_YOUR_COS_API_KEY

# -------------------------------------------------------------------------------------
# Kafka related settings
# -------------------------------------------------------------------------------------
# Kafka producer name, eg. fsdemo-data-pipeline
export KAFKA_NAME=UPDATE_TO_YOUR_PRODUCER_NAME
# Kafka topic, eg. fsdemo-data-pipeline
export KAFKA_TOPIC=UPDATE_TO_YOUR_KAFKA_TOPIC
# Kafka bootstrap servers, it is comma-separated-list, eg. server1.cloud.ibm.com:9093,server2.cloud.ibm.com:9093:server3.cloud.ibm.com:9093
export KAFKA_KAFKA_BROKERS_SASL=UPDATE_TO_YOUR_KAFKA_BOOTSTRAPE_SERVERS
# Kafka API key
export KAFKA_APIKEY=UPDATE_TO_YOUR_KAFKA_API_KEY

# Sample command to invoke the producer from command-line
java -jar $PROJECT_DIR/target/demo-dataflow-1.0-SNAPSHOT-jar-with-dependencies.jar 


