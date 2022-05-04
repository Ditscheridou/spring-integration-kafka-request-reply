# Request Reply pattern with Kafka and Spring Integration

This project is an example project for implementing a request reply pattern with Apache Kafka and Spring Integration.
It reads from a "kafkaRequests" topic, transform through a MessageConverter and writes to the "kafkaReplies" topic. 

##Setup
To set up the infrastructure navigate to the project root and run `docker-compose up -d`. This will start zookeeper, kafka
and akhq. Akhq can be accessed through `localhost:8081` but note, that the startup time can take up to 30 seconds. 

After that you can run the `DemoAppplication` class inside the project.
