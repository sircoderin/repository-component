package dot.cpp.repository.controllers;

import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.impl.module.SimpleTypeModule;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ValidationOptions;
import com.typesafe.config.Config;
import dot.cpp.repository.models.BaseEntity;
import dot.cpp.repository.mongodb.SchemaGeneratorBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Result;

public class RepositoryController extends Controller {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final Config config;

  @Inject
  public RepositoryController(Config config) {
    this.config = config;
  }

  public Result init(List<Class<? extends BaseEntity>> entities, boolean withHistory) {
    createCollections(entities, withHistory);
    return ok("init complete");
  }

  public void createCollections(List<Class<? extends BaseEntity>> entities, boolean withHistory) {
    try (final MongoClient mongoClient = new MongoClient()) {
      var database = mongoClient.getDatabase(config.getString("db.name"));
      createCollections(database, entities, withHistory);
    }
  }

  private void createCollections(
      MongoDatabase database, List<Class<? extends BaseEntity>> entities, boolean withHistory) {
    final var schemaGeneratorConfig = getSchemaGeneratorConfig();
    entities.forEach(
        entity -> {
          var schema = getSchema(entity, schemaGeneratorConfig);
          var validationOptions =
              new ValidationOptions().validator(Filters.jsonSchema(Document.parse(schema)));

          createCollection(database, entity.getSimpleName(), schema, validationOptions);

          if (withHistory) {
            createCollection(
                database, entity.getSimpleName() + "_history", schema, validationOptions);
          }

          logger.debug("{}", schema);
        });
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
      // default values are added by setting the variable in its respective class with an
      // initial value
    } else {
      database.createCollection(
          entityName, new CreateCollectionOptions().validationOptions(validationOptions));
    }
  }

  public boolean isCollectionInDatabase(String collectionName, MongoDatabase database) {
    final var collectionNames = database.listCollectionNames();
    return collectionNames.into(new ArrayList<>()).contains(collectionName);
  }

  private SchemaGeneratorConfig getSchemaGeneratorConfig() {
    final var module =
        SimpleTypeModule.forPrimitiveAndAdditionalTypes()
            .withNumberType(Byte.class)
            .withNumberType(Integer.class)
            .withNumberType(Long.class);
    return new SchemaGeneratorBuilder().withModule(module).withConstraints().withInline().build();
  }

  public String getSchema(Class<?> entityClass, SchemaGeneratorConfig schemaGeneratorConfig) {
    final var generator = new SchemaGenerator(schemaGeneratorConfig);
    final var jsonSchemaAsObjectNode = generator.generateSchema(entityClass);
    jsonSchemaAsObjectNode.remove("$schema");
    return jsonSchemaAsObjectNode.toPrettyString();
  }
}
