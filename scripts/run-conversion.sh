#!/usr/bin/env bash
cd "$(dirname "$0")/.." && exec ./mvnw -f conversion-service/pom.xml spring-boot:run
