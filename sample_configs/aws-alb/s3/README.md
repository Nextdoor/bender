## ALB logs to Firehose-Elasticsearch

### Description

Writes Application Load Balancer (ALB) logs stored in S3 to Firehose steam which pushes to an AWS hosted Elasticsearch cluster. Consult [es services docs](https://aws.amazon.com/elasticsearch-service/) on how to setup Elasticsearch service and [ALB docs](http://docs.aws.amazon.com/elasticloadbalancing/latest/application/load-balancer-access-logs.html#enable-access-logging) on how to enable logging to S3.


### Function Settings


| Lambda Setting | Value                                                         |
| -------------- | ------------------------------------------------------------- |
| runtime        | java                                                          |
| handler        | `com.nextdoor.bender.handler.kinesis.S3Handler::handler`      |
| memory         | 1536                                                          |
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
---
handler:
  type: S3Handler
  fail_on_exception: true
sources:
- name: ALB Logs
  source_regex: ".*"
  deserializer:
    type: Regex
    use_re2j: false
    regex: ([^ ]*) ([^ ]*) ([^ ]*) ([^ ]*):([0-9]*) ([^ ]*):([0-9]*) ([.0-9]*) ([.0-9]*) ([.0-9]*) (-|[0-9]*) (-|[0-9]*) ([-0-9]*) ([-0-9]*) \"([^ ]*) ([^ ]*) (- |[^ ]*)\" (\"[^\"]*\") ([A-Z0-9-]+) ([A-Za-z0-9.-]*) ([^ ]*) ([^ ]*)\s
    fields:
    - name: type
      type: STRING
    - name: timestamp
      type: STRING
    - name: elb
      type: STRING
    - name: client_ip
      type: STRING
    - name: client_port
      type: NUMBER
    - name: target_ip
      type: STRING
    - name: target_port
      type: NUMBER
    - name: request_processing_time
      type: NUMBER
    - name: target_processing_time
      type: NUMBER
    - name: elb_status_code
      type: NUMBER
    - name: target_status_code
      type: NUMBER
    - name: received_bytes
      type: NUMBER
    - name: sent_bytes
      type: NUMBER
    - name: request_verb
      type: STRING
    - name: url
      type: STRING
    - name: protocol
      type: STRING
    - name: user_agent
      type: STRING
    - name: ssl_cipher
      type: STRING
    - name: ssl_protocol
      type: STRING
    - name: target_group_arn
      type: STRING
    - name: trace_id
      type: STRING
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