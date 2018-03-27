FROM openjdk:8-jdk-alpine

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0 \
    JAVA_OPTS=""

# Set the working directory
WORKDIR /qualopt

# # Copy the current directory contents into the container
ADD . /qualopt

# Make port 8080 available to the world outside this container
EXPOSE 8080
EXPOSE 9000
EXPOSE 3001

RUN apk add --update nodejs bash

RUN npm install -g yarn

RUN ./mvnw -Pdev package

# Run app when the container launches
CMD ["/bin/bash", "-c", "yarn install && yarn start & ./mvnw"]
