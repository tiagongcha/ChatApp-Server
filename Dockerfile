FROM java:8
WORKDIR /
ADD target/HTTPServer-0.0.1-SNAPSHOT.jar HTTPServer.jar
ADD resources/ resources/
EXPOSE 8080
CMD java -jar HTTPServer.jar