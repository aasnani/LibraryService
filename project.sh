#!/bin/bash

case "$1" in
  gradle_lock)
    ./gradlew writeAllLocks --write-locks --no-configuration-cache
    ;;
  gradle_build)
    ./gradlew clean build -x test
    ;;
  gradle_test)
    ./gradlew test
    ;;
  docker_build)
    docker build -t library-service .
    ;;
  docker_run)
    docker compose up
    ;;
  docker_build_run)
    docker compose up --build
    ;;
  *)
    echo "Usage: ./project.sh {gradle_lock|gradle_build|gradle_test|gradle_build_test|docker_build|docker_run|docker_build_run}"
    exit 1
esac