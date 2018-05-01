rm -rf ./target/
mvn package
java -jar target/exchange-server-1.0-SNAPSHOT.jar db migrate exchange-server-config.yml
