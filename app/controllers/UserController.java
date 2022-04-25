package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.impl.module.SimpleTypeModule;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ValidationOptions;
import enums.UserRole;
import javax.inject.Inject;
import models.User;
import org.bson.Document;
import play.mvc.Controller;
import play.mvc.Result;
import repository.UserRepository;
import schema.JsonComponentSchemaGeneratorConfigBuilder;
import views.html.index;

public class UserController extends Controller {

  private final UserRepository userRepository;

  @Inject
  public UserController(UserRepository user) {
    this.userRepository = user;
  }

  public Result modifyUser(String id) {
    User user = userRepository.findById(id);
    return ok(index.render(user));
  }

  public Result save() {
    final User user = new User();
    user.setId("asda2");
    user.setEmail("none@yahoo.com");
    user.setUserName("john");
    user.setRole(UserRole.ADMIN);
    user.setPassword("pass");
    userRepository.save(user);
    return ok(index.render(user));
  }

  public Result get(String id) {
    return ok(userRepository.findById(id).toString());
  }

  public Result createCollection() {

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
    }
    return ok(schema);
  }

  public String getSchema(Class<?> entityClass, SchemaGeneratorConfig schemaGeneratorConfig) {
    SchemaGenerator generator = new SchemaGenerator(schemaGeneratorConfig);
    ObjectNode jsonSchemaAsObjectNode = generator.generateSchema(entityClass);
    jsonSchemaAsObjectNode.remove("$schema");
    return jsonSchemaAsObjectNode.toPrettyString();
  }
}
