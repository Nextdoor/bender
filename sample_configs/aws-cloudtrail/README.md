## CloudTrail to Firehose-Elasticsearch

### Description

Writes CloudTrail logs stored in S3 to Firehose steam which pushes to an Amazon
Elasticsearch Service cluster. Consult the [Elasticsearch Services docs]
(https://aws.amazon.com/elasticsearch-service/) on how to setup Elasticsearch
service and [CloudTrail docs](https://docs.aws.amazon.com/console/awscloudtrail/)
on how to setup CloudTrail.


### Function Settings


| Lambda Setting | Value                                                      |
| -------------- | ---------------------------------------------------------- |
| runtime        | java                                                       |
| handler        | `com.nextdoor.bender.handler.s3.S3Handler::handler`        |
| memory         | 512                                                        |
| timeout        | 300                                                        |

| Environment Vars | Value                     | Notes                        |
| ---------------- | ------------------------- | ---------------------------- |
| BENDER_CONFIG    | s3://mybucket/myfile.yaml | Your function will need IAM permissions to read this file |
| FIREHOSE_STREAM  | my-stream                 | The name of your destination firehose stream. Must be the name and not the ARN. |

### Permissions

| Type             | Value                           | Notes                  |
| ---------------- | ------------------------------- |----------------------- |
| Role             | AWSLambdaBasicExecutionRole     |                        |
| Permission       | s3:GetObject                    | Location of where Cloudtrail logs are stored in S3 |
| Permission       | firehose:DescribeDeliveryStream |                        |
| Permission       | firehose:ListDeliveryStreams    |                        |
| Permission       | firehose:PutRecord              |                        |
| Permission       | firehose:PutRecordBatch         |                        |
| Permission       | cloudwatch:PutMetricData        |                        |

### Trigger
Add a Lambda S3 trigger that triggers on object creation in the S3 bucket
containing your Cloudtrail logs.

### Configuration

See [firehose-elasticsearch.yaml](firehose-elasticsearch.yaml)