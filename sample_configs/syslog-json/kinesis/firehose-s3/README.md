## syslog-ng to Firehose-S3

### Description

Reads syslog-ng json data from a kinesis stream, performs data transformation, and writes to a firehose stream connected to an AWS hosted Elasticsearch cluster. Your data pipeline will look like:

syslog messages -> syslog-ng -> local file (json) -> kinesis agent -> kinesis -> Lambda -> Firehose -> S3

Here is an example of the syslog-ng configuration to write to json:

```
@module tfjson
destination d_mnt_json { file(
      "/mnt/log/$FACILITY.json"
      template(
        "$(format_json  --scope selected_macros --scope nv_pairs HOST=${HOST} DATE=${DATE} TIMEZONE=${TZ} EPOCH=${UNIXTIME})")
      perm(0644) owner(root) group(root) dir_perm(0755) create_dirs(yes));
     };
```


Additional Resources:

* [Amazon Kinesis Agent](https://github.com/awslabs/amazon-kinesis-agent)
* [syslog-ng](https://syslog-ng.org/)
* [syslog-ng json template function](https://www.balabit.com/documents/syslog-ng-ose-3.5-guides/en/syslog-ng-ose-guide-admin/html-single/index.html#reference-template-functions)

### Function Settings


| Lambda Setting | Value                                                         |
| -------------- | ------------------------------------------------------------- |
| runtime        | java                                                          |
| handler        | `com.nextdoor.bender.handler.kinesis.KinesisHandler::handler` |
| memory         | 512                                                           |
| timeout        | 300                                                           |

| Environment Vars | Value                     | Notes                      |
| ---------------- | ------------------------- | -------------------------- |
| BENDER_CONFIG    | s3://mybucket/myfile.yaml | Your function will need IAM permissions to read this file |
| FIREHOSE_STREAM  | my-stream                 | The name of your destination firehose stream. Must be the name and not the ARN. |

### Permissions

| Type             | Value                           | Notes                     |
| ---------------- | ------------------------------- |-------------------------- |
| Role             | AWSLambdaKinesisExecutionRole   |                           |
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
  type: KinesisHandler
  fail_on_exception: true
sources:
- deserializer:
    type: GenericJson
    nested_field_configs:
    - field: MESSAGE
      prefix_field: MESSAGE_PREFIX
  name: Syslog Messages
  operations:
  - type: TimeOperation
    time_field: $.EPOCH
    time_field_type: SECONDS
  - type: JsonKeyNameOperation
  - type: JsonDropArraysOperation
  source_regex: .*
wrapper:
  type: KinesisWrapper
serializer:
  type: Json
transport:
  type: Firehose
  stream_name: "<FIREHOSE_STREAM>"
  threads: 5
reporters:
- type: Cloudwatch
  stat_filters:
  - name: timing.ns
  - name: success.count
  - name: error.count
    report_zeros: false
```