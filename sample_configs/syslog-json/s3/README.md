## syslog-ng partitioned S3

### Description

This is an extension of the syslog-ng to [firehose-s3 pipeline]
(https://github.com/Nextdoor/bender/blob/master/sample_configs/syslog-json/firehose-s3).
In that pipeline unpartitioned data is written to S3 by Firehose. This addition
reads the produced files and partitions the data by hostname, facility, and
event time.


### Function Settings


| Lambda Setting | Value                                                      |
| -------------- | ---------------------------------------------------------- |
| runtime        | java                                                       |
| handler        | `com.nextdoor.bender.handler.s3.S3Handler::handler`        |
| memory         | 1536                                                       |
| timeout        | 300                                                        |

| Environment Vars   | Value                     | Notes                      |
| ------------------ | ------------------------- | -------------------------- |
| BENDER_CONFIG      | s3://mybucket/myfile.yaml | Your function will need IAM permissions to read this file. |
| S3\_BUCKET\_NAME   | mysyslogbucket            |                            |
| S3\_BASE\_PATH     | syslog                    | Do not use / at start of path. |

### Permissions

| Type             | Value                           | Notes                  |
| ---------------- | ------------------------------- |----------------------- |
| Role             | AWSLambdaBasicExecutionRole     |                        |
| Permission       | s3:GetObject                    | Location of where source log files are stored in S3. |
| Permission       | s3:AbortMultipartUpload         |                        |
| Permission       | s3:PutObject                    |                        |
| Permission       | s3:ListMultipartUploadParts     |                        |
| Permission       | s3:ListBucketMultipartUploads   |                        |
| Permission       | cloudwatch:PutMetricData        |                        |

### Trigger
Add a Lambda S3 trigger that triggers on object creation in the S3 bucket that
Firehose writes data to.

### Configuration

```
handler:
  type: S3Handler
  fail_on_exception: true
sources:
- deserializer:
    type: GenericJson
  name: Logs
  source_regex: .*
  operations:
  - type: PartitionOperation
    partition_specs:
    - name: host
      sources:
      - $.payload.HOST
      interpreter: STRING
    - name: facility
      sources:
      - $.payload.FACILITY
      interpreter: STRING
    - format: YYYY-MM-dd-HH
      interpreter: SECONDS
      name: ds
      sources:
      - $.payload.EPOCH
wrapper:
  type: PassthroughWrapper
serializer:
  type: Json
transport:
  type: S3
  base_path: <S3_BASE_PATH>
  bucket_name: <S3_BUCKET_NAME>
  compress_buffer: false
  max_buffer_size: 134217728
  threads: 5
  use_compression: true
reporters:
- type: Cloudwatch
  stat_filters:
  - name: timing.ns
  - name: success.count
  - name: error.count
    report_zeros: false
```