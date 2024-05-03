package dot.cpp.repository.helper;

import dot.cpp.repository.models.BaseEntity;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public final class BaseHelper {
  private BaseHelper() {}

  @NotNull
  public static Set<String> getRecordIds(List<? extends BaseEntity> entities) {
    return entities.stream().map(BaseEntity::getRecordId).collect(Collectors.toSet());
  }

  public static boolean caseInsensitiveContains(String parent, String child) {
    return parent.toLowerCase().contains(child.toLowerCase());
  }
}
