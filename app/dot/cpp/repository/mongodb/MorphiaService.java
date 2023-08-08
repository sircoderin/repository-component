package dot.cpp.repository.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.typesafe.config.Config;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MorphiaService {

  private final Datastore datastore;

  @Inject
  public MorphiaService(Config config) {
    final var mongoClientSettings =
        MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(config.getString("morphia.uri")))
            .build();
    datastore = Morphia.createDatastore(MongoClients.create(mongoClientSettings));
  }

  public Datastore datastore() {
    return datastore;
  }
}
