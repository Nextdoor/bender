## syslog-ng to Elasticsearch Service

### Description

Reads syslog-ng json data from a Kinesis Stream, performs data transformation,
and writes to an Amazon Elasticsearch Service cluster. Your data pipeline will
look like:

syslog messages -> syslog-ng -> local file (json) -> kinesis agent -> kinesis
-> Lambda -> Elasticsearch Service


It should be noted that this example contains the `JsonDropArraysOperation`
operation because Elasticsearch handles arrays in a peculiar manner not
conducive to exploration. It is suggested that you do not send arrays as part
of your log data.

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
* [Elasticsearch Service](https://aws.amazon.com/elasticsearch-service/)
* [Elasticserach Service IAM](http://docs.aws.amazon.com/elasticsearch-service/latest/developerguide/es-createupdatedomains.html#es-createdomain-configure-access-policies)
* [syslog-ng](https://syslog-ng.org/)
* [syslog-ng json template function](https://www.balabit.com/documents/syslog-ng-ose-3.5-guides/en/syslog-ng-ose-guide-admin/html-single/index.html#reference-template-functions)

### Function Settings


| Lambda Setting | Value                                                      |
| -------------- | ---------------------------------------------------------- |
| runtime        | java                                                       |
| handler        | `com.nextdoor.bender.handler.kinesis.KinesisHandler::handler` |
| memory         | 512                                                        |
| timeout        | 300                                                        |

| Environment Vars | Value                     | Notes                        |
| ---------------- | ------------------------- | ---------------------------- |
| BENDER_CONFIG    | s3://mybucket/myfile.yaml | Your function will need IAM permissions to read this file |
| ES_HOSTNAME      | ...es.amazonaws.com       |                              |
| ES_REGION        |                           | For example US_EAST_1        |


### Permissions

| Type             | Value                           | Notes                  |
| ---------------- | ------------------------------- |----------------------- |
| Role             | AWSLambdaKinesisExecutionRole   |                        |
| Permission       | es:ESHttpPut                    | `arn:aws:es:<region>:<aws_account_id>:domain/<domain-name>/<sub-resource>/*` |
| Permission       | cloudwatch:PutMetricData        |                        |

### Trigger
Add a Lambda Kinesis trigger that links to the Kinesis stream the Agent is
publishing to.

### Configuration

See [elasticsearch.yaml](elasticsearch.yaml)
