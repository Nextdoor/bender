[![CircleCI](https://circleci.com/gh/Nextdoor/bender.svg?style=svg)](https://circleci.com/gh/Nextdoor/bender)

# Bender - Serverless ETL Framework


## About

This project provides an extendable Java framework for creating serverless ETL
functions on [AWS Lambda](https://aws.amazon.com/lambda/). Bender Core handles
the complex plumbing and provides the interfaces necessary to build modules for
all aspects of the ETL process.


### Initial Support

While developers are able to write their own Input Handlers, Deserializers,
Mutators, Wrappers, Serializers, Transporters, or Reporters, out of box the
Bender contains basic functionality to read, filter, manipulate, and write JSON
from [Amazon Kinesis Streams](https://aws.amazon.com/kinesis/streams/) or
[Amazon S3](https://aws.amazon.com/s3/) files. Specially the following is
supported:

##### Handlers
Handlers interface between Amazon Lambda
[triggers](http://docs.aws.amazon.com/lambda/latest/dg/invoking-lambda-function.html)
and data sources provided to your ETL function. Events or lines are able to
read from:

* Kinesis
* S3
* S3 via SNS

##### Pre Deserialization Filters
Modular filter support is not yet included but basic string matching and regex
based filters are included as a part of Bender Core.

##### Deserializers
Included is a generic JSON deserializer to transform strings into GSON objects.
This allows processing of loosely defined schemas such as those done in
application logging. For schema validation the use of GSON to POJOs is
encouraged.

##### Mutators
Data sometimes needs to be unwrapped, fixed, sanitized, or enriched. Mutators
allow for these types of data manipulation. Included JSON mutators:

* Root node promoter
* Array Dropping
* Appending value type information to key names

##### Wrappers
Optionally wrap data to provide additional information on where the data
originated from and what processed it:

* Kinesis Wrapper
* S3 Wrapper
* Basic Wrapper

##### Serializers
Write your mutated and wrapped data back into JSON before loading it elsewhere.

##### Transporters
Transporters convert string payloads to serialized wire formats and send
batches of data to destinations.

* Firehose
* S3 (partitioning support included)
* Elasticsearch (time based index inserts)

##### Reporters
Monitor the ETL process at each phase within your function and output those
metrics for easy consumption in

* Cloudwatch Metrics
* Datadog

## Configuration

Bender uses JSON configuration files. See the
[docs](https://pages.github.com/Nextdoor/bender/) for more details on config
file specifics.

Different configurations per lambda function alias are supported. The filename
of the configuration must match the function alias.

## Deployment

The easiest way to deploy your function is to use
[Apex](https://github.com/apex/apex). A sample project is included under
[example_project/](https://github.com/Nextdoor/bender/tree/master/example_project).
The project provides an example of a function that is triggered by Kinesis,
drops data matching a regex, and forwards the rest to Firehose.

Note to deploy the example you will need to create an IAM role to allow your
lambda function to read from kinesis and write to firehose. Your role will need
the following two policies:

`arn:aws:iam::aws:policy/service-role/AWSLambdaKinesisExecutionRole`
`arn:aws:iam::aws:policy/AmazonKinesisFirehoseFullAccess`

After creating your role edit `example_project/project.json` with the role ARN.
You will also need to create the source Kinesis and destination Firehose
streams.

To deploy:

    cd example_project/
    make deploy
    DRY=false make deploy


## Contributing
Features and bug fixes are welcome. Please adhere to the following guidelines:

- Use Google's Java [style guide](https://github.com/google/styleguide) for your IDE.
- Be conscientious of dependencies you add to Core.
- Help maintain unit test code coverage by adding tests for each branch in new code.
