# demo-data-pipeline-w-code-engine
This demo shows how data files written to COS buckets can be automatically processed through the data pipeline using services in IBM Cloud. The following steps are involved in the data pipeline:

* Data file is uploaded to COS bucket.
* Code engine job is triggered by COS write event (IBM Cloud container registry holds the image for the job).
* Code engine job reads data file from COS bucket, and push data records to Kafka topic (producer). Note that this step is optional in batch data pipeline, and code engine jobs can process the data and make it ready for consumption. It is included here to demonstrate how to push data records to Kafka, since some 3rd party software/services has connector to Kafka already.
* Data handler (consumer) consumes data records from Kafka topic, process the data if needed, and save the result back to COS bucket
* Various applications can make use of the data

Refer to this [architecture diagram](chart/data-pipeline.jpg).

For more detailed description, please refer to the [blog post](link).

For quick command reference, please refer to the [sample commands](bin/reference_commands.sh) in this project.
