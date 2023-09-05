package dot.cpp.repository.services;

import static com.mongodb.client.model.Indexes.ascending;
import static dot.cpp.repository.models.BaseEntity.RECORD_ID;

import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.impl.module.SimpleTypeModule;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ValidationOptions;
import com.typesafe.config.Config;
import dot.cpp.repository.models.BaseEntity;
import dot.cpp.repository.models.Index;
import dot.cpp.repository.mongodb.SchemaGeneratorBuilder;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RepositoryService {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final String mongoUri;
  private final String database;
  private final SchemaGenerator schemaGenerator;

  @Inject
  public RepositoryService(Config config) {
    this.mongoUri = config.getString("morphia.uri");
    this.database = config.getString("morphia.database");
    this.schemaGenerator = getSchemaGenerator();
  }

  @SafeVarargs
  private void createCollections(
      MongoDatabase database, boolean withHistory, Class<? extends BaseEntity>... entities) {
    for (var entity : entities) {
      var schema = getSchema(entity);
      var validationOptions =
          new ValidationOptions().validator(Filters.jsonSchema(Document.parse(schema)));

      createCollection(database, entity.getSimpleName(), schema, validationOptions);
      createIndexes(database, Index.from(entity, RECORD_ID, ascending(RECORD_ID), true));

      if (withHistory) {
        createCollection(database, entity.getSimpleName() + "_history", schema, validationOptions);
        createIndexes(
            database,
            Index.from(
                entity.getSimpleName() + "_history", RECORD_ID, ascending(RECORD_ID), false));
      }

      logger.debug("{}", schema);
    }
  }

  @SafeVarargs
  public final void createCollections(Class<? extends BaseEntity>... entities) {
    try (final var mongoClient = new MongoClient(mongoUri)) {
      createCollections(getDatabase(mongoClient), false, entities);
    }
  }

  @SafeVarargs
  public final void createCollectionsWithHistory(Class<? extends BaseEntity>... entities) {
    try (final var mongoClient = new MongoClient(mongoUri)) {
      createCollections(getDatabase(mongoClient), true, entities);
    }
  }

  public void createIndexes(Index... indexes) {
    try (final var mongoClient = new MongoClient(mongoUri)) {
      createIndexes(getDatabase(mongoClient), indexes);
    }
  }

  private void createIndexes(MongoDatabase database, Index... indexes) {
    for (var idx : indexes) {
      final var collection = database.getCollection(idx.collectionName);

      for (final var index : collection.listIndexes()) {
        if (index.getString("name").equals(idx.name)) {
          collection.dropIndex(idx.name);
          break;
        }
      }

      collection.createIndex(
          idx.specification, new IndexOptions().name(idx.name).unique(idx.unique));
    }
  }

  public void emptyCollection(Class<? extends BaseEntity> entity) {
    try (final var mongoClient = new MongoClient(mongoUri)) {
      final var db = getDatabase(mongoClient);
      db.getCollection(entity.getSimpleName()).drop();
      createCollections(db, false, entity);
    }
  }

  public boolean isCollectionInDatabase(String collectionName) {
    try (final var mongoClient = new MongoClient(mongoUri)) {
      return isCollectionInDatabase(collectionName, getDatabase(mongoClient));
    }
  }

  private boolean isCollectionInDatabase(String collectionName, MongoDatabase database) {
    final var collectionNames = database.listCollectionNames();
    return collectionNames.into(new ArrayList<>()).contains(collectionName);
  }

  public String getSchema(Class<? extends BaseEntity> entityClass) {
    final var jsonSchemaAsObjectNode = schemaGenerator.generateSchema(entityClass);
    jsonSchemaAsObjectNode.remove("$schema");
    return jsonSchemaAsObjectNode.toPrettyString();
  }

  private void createCollection(
      MongoDatabase database,
      String entityName,
      String schema,
      ValidationOptions validationOptions) {
    if (isCollectionInDatabase(entityName, database)) {
      logger.debug("already exists");

      database.runCommand(
          new Document("collMod", entityName)
              .append("validator", Filters.jsonSchema(Document.parse(schema)))
              .append("validationLevel", "strict"));
    } else {
      database.createCollection(
          entityName, new CreateCollectionOptions().validationOptions(validationOptions));
    }
  }

  private MongoDatabase getDatabase(MongoClient mongoClient) {
    return mongoClient.getDatabase(database);
  }

  private static SchemaGenerator getSchemaGenerator() {
    final var module =
        SimpleTypeModule.forPrimitiveAndAdditionalTypes()
            .withNumberType(Byte.class)
            .withNumberType(Integer.class)
            .withNumberType(Long.class);
    return new SchemaGenerator(
        new SchemaGeneratorBuilder().withModule(module).withConstraints().withInline().build());
  }
}
