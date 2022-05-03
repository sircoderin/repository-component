package dot.cpp.repository.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
import dot.cpp.repository.models.Customer;
import dot.cpp.repository.models.User;
import dot.cpp.repository.mongodb.JsonComponentSchemaGeneratorConfigBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Result;

public class InitController extends Controller {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final Config config;

  @Inject
  public InitController(Config config) {
    this.config = config;
  }

  public Result init() {
    createDatabase();
    return ok("init");
  }

  public void createDatabase() {
    try (final MongoClient mongoClient = new MongoClient()) {
      var database = mongoClient.getDatabase(config.getString("db.name"));

      var entities = List.of(User.class, Customer.class);

      createCollections(entities, database);
    }
  }

  private void createCollections(
      List<Class<? extends BaseEntity>> entities, MongoDatabase database) {
    final var schemaGeneratorConfig = getSchemaGeneratorConfig();

    entities.forEach(
        entity -> {
          var schema = getSchema(entity, schemaGeneratorConfig);
          var validationOptions =
              new ValidationOptions().validator(Filters.jsonSchema(Document.parse(schema)));

          if (isCollectionInDatabase(entity.getSimpleName(), database)) {
            database.runCommand(
                new Document("collMod", entity.getSimpleName())
                    .append("validator", Document.parse(schema))
                    .append("validationLevel", "strict"));
            // defaults are added by setting the variable in its respective class with an initial value
            logger.debug("already exists");
          } else {
            database.createCollection(
                entity.getSimpleName(),
                new CreateCollectionOptions().validationOptions(validationOptions));
          }

          logger.debug("{}", schema);
        });
  }

  public boolean isCollectionInDatabase(String collectionName, MongoDatabase database) {
    var collectionNames = database.listCollectionNames();

    return collectionNames.into(new ArrayList<>()).contains(collectionName);
  }

  private SchemaGeneratorConfig getSchemaGeneratorConfig() {
    var module =
        SimpleTypeModule.forPrimitiveAndAdditionalTypes().withIntegerType(Byte.class, "integer");
    var configBuilder =
        new JsonComponentSchemaGeneratorConfigBuilder()
            .withModule(module)
            .withConstraints()
            .withInline();
    return configBuilder.build();
  }

  public String getSchema(Class<?> entityClass, SchemaGeneratorConfig schemaGeneratorConfig) {
    SchemaGenerator generator = new SchemaGenerator(schemaGeneratorConfig);
    ObjectNode jsonSchemaAsObjectNode = generator.generateSchema(entityClass);
    jsonSchemaAsObjectNode.remove("$schema");
    return jsonSchemaAsObjectNode.toPrettyString();
  }
}
