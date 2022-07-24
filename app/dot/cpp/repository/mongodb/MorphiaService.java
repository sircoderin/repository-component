package dot.cpp.repository.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.typesafe.config.Config;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MorphiaService {

  private final MongoClient mongo;
  private final Datastore datastore;

  @Inject
  public MorphiaService(Config config) {
    mongo = MongoClients.create(config.getString("morphia.uri"));
    datastore = Morphia.createDatastore(mongo, config.getString("morphia.database"));
    datastore.getMapper().mapPackage(config.getString("morphia.package"));
    datastore.ensureIndexes();
  }

  public MongoClient mongo() {
    return mongo;
  }

  public Datastore datastore() {
    return datastore;
  }
}
