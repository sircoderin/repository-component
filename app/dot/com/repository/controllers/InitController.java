package dot.com.repository.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.impl.module.SimpleTypeModule;
import com.mongodb.MongoClient;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ValidationOptions;
import com.typesafe.config.Config;
import dot.com.repository.models.User;
import dot.com.repository.mongodb.JsonComponentSchemaGeneratorConfigBuilder;
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
    createCollection();
    return ok("init");
  }

  public void createCollection() {

    final var schemaGeneratorConfig = getSchemaGeneratorConfig();
    final var schema = getSchema(User.class, schemaGeneratorConfig);

    try (final MongoClient mongoClient = new MongoClient()) {
      var database = mongoClient.getDatabase(config.getString("db.name"));
      var validationOptions =
          new ValidationOptions().validator(Filters.jsonSchema(Document.parse(schema)));

      database.createCollection(
          "User", new CreateCollectionOptions().validationOptions(validationOptions));

      logger.debug("{}", schema);
    }
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
