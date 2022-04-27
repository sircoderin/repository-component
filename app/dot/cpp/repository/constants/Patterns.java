package dot.cpp.repository.constants;

public class Patterns {

  public static final String NAME = "^([ \\u00c0-\\u01ffa-zA-Z'\\-])+$";

  public static final String PHONE_NUMBER =
      "(?=^.{10,60}$)^[+]*[(]{0,1}[0-9]{1,4}[)]{0,1}[-\\s\\./0-9]*$";

  public static final String EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,10}$";

  public static final String UUID =
      "^$|[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";

  public static final String ACCESS_TOKEN = "[\\.\\_\\-0-9a-zA-Z]{32,}";

  public static final String ALPHA_LIMITED_RO_MAX70 = "[0-9\\p{L}\\-\\.\\_\\s]{1,70}";

  public static final String ALPHA_PASS_MIN8 =
      "(?=^.{8,64}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*]).*$";
}
