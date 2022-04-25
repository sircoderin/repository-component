package dot.com.repository.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import dot.com.repository.models.User;
import dot.com.repository.repository.UserRepository;
import dot.com.repository.enums.UserRole;
import javax.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;
import dot.com.repository.views.html.index;

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

  public String getSchema(Class<?> entityClass, SchemaGeneratorConfig schemaGeneratorConfig) {
    SchemaGenerator generator = new SchemaGenerator(schemaGeneratorConfig);
    ObjectNode jsonSchemaAsObjectNode = generator.generateSchema(entityClass);
    jsonSchemaAsObjectNode.remove("$schema");
    return jsonSchemaAsObjectNode.toPrettyString();
  }
}
