## CloudTrail to Firehose-Elasticsearch

### Description

Writes CloudTrail logs stored in S3 to Firehose steam which pushes to an AWS hosted Elasticsearch cluster. Consult [Elasticsearch Services docs](https://aws.amazon.com/elasticsearch-service/) on how to setup Elasticsearch service and [CloudTrail docs](https://docs.aws.amazon.com/console/awscloudtrail/) on how to setup CloudTrail.


### Function Settings


| Lambda Setting | Value                                                         |
| -------------- | ------------------------------------------------------------- |
| runtime        | java                                                          |
| handler        | `com.nextdoor.bender.handler.kinesis.S3Handler::handler` |
| memory         | 512                                                           |
| timeout        | 300                                                           |

| Environment Vars | Value                     | Notes                      |
| ---------------- | ------------------------- | -------------------------- |
| BENDER_CONFIG    | s3://mybucket/myfile.yaml | Your function will need IAM permissions to read this file |
| FIREHOSE_STREAM  | my-stream                 | The name of your destination firehose stream. Must be the name and not the ARN. |

### Permissions

| Type             | Value                           | Notes                     |
| ---------------- | ------------------------------- |-------------------------- |
| Role             | AWSLambdaBasicExecutionRole     |                           |
| Permission       | s3:GetObject                    | Location of where Cloudtrail logs are stored in S3 |
| Permission       | firehose:DescribeDeliveryStream |                           |
| Permission       | firehose:ListDeliveryStreams    |                           |
| Permission       | firehose:PutRecord              |                           |
| Permission       | firehose:PutRecordBatch         |                           |
| Permission       | cloudwatch:PutMetricData        |                           |

### Trigger
Add a Lambda S3 trigger that triggers on object creation in the S3 bucket containing your Cloudtrail logs.

### Configuration

```
handler:
  type: S3Handler
  fail_on_exception: true
sources:
- name: Cloudtrail
  source_regex: ".*"
  deserializer:
    type: GenericJson
  operations:
  - type: JsonRootNodeOperation
    root_path: "$.Records"
  - type: JsonArraySplitOperation
  - type: JsonKeyNameOperation
wrapper:
  type: PassthroughWrapper
serializer:
  type: Json
transport:
  type: Firehose
  threads: 5
  append_newline: false
  firehose_buffer: SIMPLE
  stream_name: "<FIREHOSE_STREAM>"
reporters:
- type: Cloudwatch
  stat_filters:
  - name: timing.ns
  - name: success.count
  - name: error.count
    report_zeros: false
```