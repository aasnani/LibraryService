#!/bin/bash

case "$1" in
  resolve)
    ./gradlew writeAllLocks --write-locks --no-configuration-cache
    ;;
  build)
    ./gradlew build
    ;;
  test)
    ./gradlew test
    ;;
  build_image)
    docker build -t library-service .
    ;;
  run)
    docker compose up --build
    ;;
  *)
    echo "Usage: ./project.sh {resolve|build|build_image|test|run}"
    exit 1
esac