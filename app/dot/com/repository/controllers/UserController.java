package dot.com.repository.controllers;

import dot.com.repository.enums.UserRole;
import dot.com.repository.models.User;
import dot.com.repository.repository.UserRepository;
import dot.com.repository.views.html.index;
import javax.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;

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
    user.setId("asda1");
    user.setEmail("none@yahoo.com");
    user.setUserName("john");
    user.setRole(UserRole.ADMIN);
    user.setPassword("pass");
    userRepository.save(user);
    return ok(index.render(user));
  }

  public Result findByValue(String field, String value) {
    return ok(userRepository.findByField(field, value).toString());
  }

  public Result get(String id) {
    return ok(userRepository.findById(id).toString());
  }
}
