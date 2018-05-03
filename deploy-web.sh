#!/bin/bash

# usage: ./deploy.sh <profile-name>
# where profile-name corresponds to a profile in your ~/.aws/credentials file
# upload appropriate files to S3. TODO: change to using terraform with state in an S3 bucket

aws s3 cp index.html s3://qasrl.org/index.html --profile $1
aws s3 cp error.html s3://qasrl.org/error.html --profile $1