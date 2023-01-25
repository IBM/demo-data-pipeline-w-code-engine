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

# -------------------------------------------------------------------------------------
# This file contains the CLI reference commands for building container image and deploying code engine job.
# Refer to the instructions in the blog for the detailed steps, and you can reference 
# this file for the commands when you need to perform certain tasks.
# -------------------------------------------------------------------------------------

# Settings used for this demo, feel free to update the values based on your environment

# Project directory
PROJECT_DIR=UPDATE_TO_YOUR_PROJECT_DIR
# Local image name
LOCAL_IMAGE_NAME=yytest_datapipeline

# settings in cloud
CLOUD_SERVICE_ID=YOUR_SERVICE_ID
CLOUD_RESOURCE_GROUP=yytest-rg
CLOUD_CONTAINER_REGISTRY_NAMESPACE=yytest
CLOUD_CONTAINER_REGISTRY_IMAGE_NAME=datapipeline
CLOUD_COS_INSTANCE=yytest-cos
CLOUD_EVENT_STREAM_INSTANCE=yytest-Event-Streams
CLOUD_CODE_ENGINE_PROJECT_NAME=cetest
CLOUD_CODE_ENGINE_PROJECT_CONFIGMAP=datapipelineconfigmap
CLOUD_CODE_ENGINE_JOB_NAME=datapipeline-job
CLOUD_CODE_ENGINE_JOBRUN_NAME=datapipeline-jobrun
CLOUD_CODE_ENGINE_COS_PREFIX=COS
CLOUD_CODE_ENGINE_EVENT_STREAM_PREFIX=KAFKA

# -------------------------------------------------------------------------------------
# Build docker image
# -------------------------------------------------------------------------------------
cd $PROJECT_DIR
# Build docker image
docker build -t $LOCAL_IMAGE_NAME .

# list images
docker images

# -------------------------------------------------------------------------------------
# Push the image to IBM Cloud Container Registry
#
# For IBM Cloud Container Registry CLI, refer to the following link: 
# https://cloud.ibm.com/docs/Registry?topic=container-registry-cli-plugin-containerregcli
# -------------------------------------------------------------------------------------
# login to ibmcloud
ibmcloud login -a cloud.ibm.com

# set resource group
ibmcloud target -g $MY_RESOURCE_GROUP

# log in to IBM container registry
ibmcloud cr login

# list namespaces
ibmcloud cr namespace-list

# create namespace in the registry
ibmcloud cr namespace-add $CLOUD_CONTAINER_REGISTRY_NAMESPACE

# To remove namespace in the registry, if needed
ibmcloud cr namespace-rm $CLOUD_CONTAINER_REGISTRY_NAMESPACE

# Tag the image
docker tag $LOCAL_IMAGE_NAME icr.io/$CLOUD_CONTAINER_REGISTRY_NAMESPACE/$CLOUD_CONTAINER_REGISTRY_IMAGE_NAME:latest

# Push the image to the registry
docker push icr.io/$CLOUD_CONTAINER_REGISTRY_NAMESPACE/$CLOUD_CONTAINER_REGISTRY_IMAGE_NAME:latest

# list images
ibmcloud cr images

# For help with the cr command
ibmcloud cr help


# Grant code engine access to container registry: 
#https://cloud.ibm.com/docs/codeengine?topic=codeengine-add-registry

# -------------------------------------------------------------------------------------
# Create code engine project
# -------------------------------------------------------------------------------------

# list options with code-engine command / For help with code-engine command
ibmcloud code-engine project help

# List existing code engine project
ibmcloud code-engine project list

# To create a code engine project
ibmcloud code-engine project create -name $CODE_ENGINE_PROJECT_NAME

# To delete a code engine project, if needed
ibmcloud code-engine project delete -name $CODE_ENGINE_PROJECT_NAME

# select the code engine project as current project
ibmcloud code-engine project select --name $CODE_ENGINE_PROJECT_NAME

# -------------------------------------------------------------------------------------
# Manage service IDs
# -------------------------------------------------------------------------------------
# list service IDs
ibmcloud iam service-ids

# Check the current ce project
ibmcloud ce project current 

# Configuring a project with a custom service ID
ibmcloud ce project update --binding-service-id $CLOUD_SERVICE_ID

# -------------------------------------------------------------------------------------
# Deploy Code Engine job
# Code Engine tutorial: https://cloud.ibm.com/docs/solution-tutorials?topic=solution-tutorials-text-analysis-code-engine
# -------------------------------------------------------------------------------------

# create or update configmap, env variable settings
ibmcloud ce configmap create --name $CLOUD_CODE_ENGINE_PROJECT_CONFIGMAP --from-env-file config.txt
ibmcloud ce configmap update --name $CLOUD_CODE_ENGINE_PROJECT_CONFIGMAP --from-env-file config.txt

# check the content of the configmap
ibmcloud ce configmap get -n $CLOUD_CODE_ENGINE_PROJECT_CONFIGMAP

# Deploy or update code engine job
ibmcloud code-engine job create --name $CLOUD_CODE_ENGINE_JOB_NAME --image icr.io/$CLOUD_CONTAINER_REGISTRY_NAMESPACE/$CLOUD_CONTAINER_REGISTRY_IMAGE_NAME:latest --registry-secret ibm-container-registry --env-from-configmap $CLOUD_CODE_ENGINE_PROJECT_CONFIGMAP
ibmcloud code-engine job update --name $CLOUD_CODE_ENGINE_JOB_NAME --image icr.io/$CLOUD_CONTAINER_REGISTRY_NAMESPACE/$CLOUD_CONTAINER_REGISTRY_IMAGE_NAME:latest --registry-secret ibm-container-registry --env-from-configmap $CLOUD_CODE_ENGINE_PROJECT_CONFIGMAP

# Bind service instance to code engine app or job
# Refer to https://cloud.ibm.com/docs/codeengine?topic=codeengine-bind-services

# list existing services
ibmcloud resource service-instances | grep yytest

# Bind to COS service with prefix COS
ibmcloud ce job bind --name $CLOUD_CODE_ENGINE_JOB_NAME --service-instance $CLOUD_COS_INSTANCE --prefix $CLOUD_CODE_ENGINE_COS_PREFIX --no-wait

# Bind to event Streams with prefix KAFKA
ibmcloud ce job bind --name $CLOUD_CODE_ENGINE_JOB_NAME --service-instance $CLOUD_EVENT_STREAM_INSTANCE --prefix $CLOUD_CODE_ENGINE_EVENT_STREAM_PREFIX --no-wait

# get details of the job
ibmcloud code-engine job get --name $CLOUD_CODE_ENGINE_JOB_NAME

# list job
ibmcloud ce job list

# submit job
ibmcloud code-engine jobrun submit --name $CLOUD_CODE_ENGINE_JOBRUN_NAME --job $CLOUD_CODE_ENGINE_JOB_NAME

# check jobrun status
ibmcloud code-engine jobrun get --name $CLOUD_CODE_ENGINE_JOBRUN_NAME

# get logs
ibmcloud code-engine jobrun logs --instance $CLOUD_CODE_ENGINE_JOBRUN_NAME

# resubmit job
ibmcloud code-engine jobrun resubmit --jobrun $CLOUD_CODE_ENGINE_JOBRUN_NAME

# automate the job, assigning the Notifications Manager role to Code Engine
#https://cloud.ibm.com/docs/codeengine?topic=codeengine-eventing-cosevent-producer#notify-mgr-cos

# -------------------------------------------------------------------------------------
# Build project and run locally
# -------------------------------------------------------------------------------------
# Build project
cd $PROJECT_DIR
mvn clean compile assembly:single

# start consumer locally and listen for messages (update the settings first)
$PROJECT_DIR/bin/script_consumer.sh

# run producer locally and pick up settings from command-line (update the settings first)
$PROJECT_DIR/bin/script_producer.sh

# run producer locally and pick up settings from environment variables (update the settings first)
$PROJECT_DIR/bin/script_producer_env.sh
