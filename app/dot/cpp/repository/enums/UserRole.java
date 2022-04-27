package dot.cpp.repository.enums;

public enum UserRole {
  USER("USER"),
  ADMIN("ADMIN");

  private final String value;

  UserRole(final String value) {

    this.value = value;
  }

  public String getValue() {

    return this.value;
  }
}
