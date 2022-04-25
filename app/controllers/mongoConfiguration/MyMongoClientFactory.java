package controllers.mongoConfiguration;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.typesafe.config.Config;
import it.unifi.cerm.playmorphia.MongoClientFactory;
import java.util.List;

public class MyMongoClientFactory extends MongoClientFactory {

  private final Config config;

  public MyMongoClientFactory(Config config) {
    super(config);
    this.config = config;
  }

  public MongoClient createClient() throws Exception {
    return new MongoClient(List.of(new ServerAddress("localhost", 27017)));
  }

  public String getDBName() {
    return config.getString("playmorphia.database");
  }
}
