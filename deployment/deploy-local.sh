#!/bin/bash

# Build jar file from gradle
cd ..
./gradlew clean build

# Build docker image
docker build -t meli-coupons-api .

# Deploy api
cd deployment
docker-compose up
