package dot.com.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.impl.module.SimpleTypeModule;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ValidationOptions;
import dot.com.models.User;
import dot.com.mongodb.JsonComponentSchemaGeneratorConfigBuilder;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Result;

public class InitController extends Controller {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  public Result init() {
    createCollection();
    return ok("init");
  }

  public void createCollection() {
    var module =
        SimpleTypeModule.forPrimitiveAndAdditionalTypes().withIntegerType(Byte.class, "integer");

    var configBuilder =
        new JsonComponentSchemaGeneratorConfigBuilder()
            .withModule(module)
            .withConstraints()
            .withInline();
    var schemaGeneratorConfig = configBuilder.build();
    var schema = getSchema(User.class, schemaGeneratorConfig);

    try (final MongoClient mongoClient = new MongoClient()) {
      MongoDatabase database = mongoClient.getDatabase("myDb");
      ValidationOptions collOptions =
          new ValidationOptions().validator(Filters.jsonSchema(Document.parse(schema)));

      database.createCollection(
          "User", new CreateCollectionOptions().validationOptions(collOptions));

      logger.debug("{}", schema);
    }
  }

  public String getSchema(Class<?> entityClass, SchemaGeneratorConfig schemaGeneratorConfig) {
    SchemaGenerator generator = new SchemaGenerator(schemaGeneratorConfig);
    ObjectNode jsonSchemaAsObjectNode = generator.generateSchema(entityClass);
    jsonSchemaAsObjectNode.remove("$schema");
    return jsonSchemaAsObjectNode.toPrettyString();
  }
}
