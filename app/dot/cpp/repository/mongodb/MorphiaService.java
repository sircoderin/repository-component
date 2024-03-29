package dot.cpp.repository.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.typesafe.config.Config;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bson.codecs.configuration.CodecRegistries;

@Singleton
public class MorphiaService {

  private final MongoClient mongo;
  private final Datastore datastore;

  @Inject
  public MorphiaService(Config config) {
    final var mongoClientSettings =
        MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(config.getString("morphia.uri")))
            .codecRegistry(
                CodecRegistries.fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromCodecs(new YearMonthCodec())))
            .build();
    final var mapperOptions = MapperOptions.builder().mapSubPackages(true).build();

    mongo = MongoClients.create(mongoClientSettings);
    datastore = Morphia.createDatastore(mongo, config.getString("morphia.database"), mapperOptions);
    datastore.getMapper().mapPackage(config.getString("morphia.login.models"));
    datastore.getMapper().mapPackage(config.getString("morphia.terra.models"));
  }

  public MongoClient mongo() {
    return mongo;
  }

  public Datastore datastore() {
    return datastore;
  }
}
