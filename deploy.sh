#!/bin/bash

# usage: ./deploy.sh
# upload the website to S3.

# TODO: change to using terraform with state in an S3 bucket?
# that would maybe delete files as appropriate.

aws s3 sync site/qasrl.org s3://qasrl.org/ \
  --exclude **/.DS_Store \
  --exclude **/.gitignore \
  --profile cse-julian

aws s3 sync site/browse.qasrl.org s3://browse.qasrl.org/ \
  --exclude **/.DS_Store \
  --exclude **/.gitignore \
  --profile cse-julian
