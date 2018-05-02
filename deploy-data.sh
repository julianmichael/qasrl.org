#!/bin/bash

# usage: ./deploy.sh <profile-name>
# where profile-name corresponds to a profile in your ~/.aws/credentials file
# upload appropriate files to S3. TODO: change to using terraform with state in an S3 bucket

tar cf data/qasrl-v2.tar data/qasrl-v2
aws s3 mv data/qasrl-v2.tar s3://qasrl.org/data/qasrl-v2.tar --profile $1