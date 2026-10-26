#!/bin/bash
echo "Initializing LocalStack S3 buckets..."

awslocal s3 mb s3://resource-service

echo "S3 bucket 'resource-service' created successfully"