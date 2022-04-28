package dot.cpp.repository.models;

import dev.morphia.annotations.Entity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
public class Customer extends BaseEntity {

  @NotBlank private String userName;

  @NotNull
  // @Pattern(regexp = Patterns.ALPHA_PASS_MIN8, message = "constraints.field.invalid")
  private String password;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
