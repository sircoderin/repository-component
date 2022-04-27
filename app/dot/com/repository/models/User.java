package dot.com.repository.models;

import dot.com.repository.constants.Patterns;
import dev.morphia.annotations.Entity;
import dot.com.repository.enums.UserRole;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Entity
public class User extends BaseEntity {

  @NotBlank private String userName;

  @NotNull
  @Pattern(regexp = Patterns.ALPHA_PASS_MIN8, message = "constraints.field.invalid")
  private String password;

  @NotNull private UserRole role;

  private List<String> groups;

  @NotNull
  @Pattern(regexp = Patterns.EMAIL, message = "constraints.field.invalid")
  private String email;

  @Pattern(regexp = Patterns.UUID, message = "constraints.field.invalid")
  private String resetPasswordUuid;

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

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public List<String> getGroups() {
    return groups;
  }

  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getResetPasswordUuid() {
    return resetPasswordUuid;
  }

  public void setResetPasswordUuid(String resetPasswordUuid) {
    this.resetPasswordUuid = resetPasswordUuid;
  }
}
