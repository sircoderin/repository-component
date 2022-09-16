package dot.cpp.repository.repository;

import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import dot.cpp.repository.models.BaseEntity;
import dot.cpp.repository.mongodb.MorphiaService;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import javax.inject.Inject;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseRepository<T extends BaseEntity> {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Inject private MorphiaService morphia;

  public T findById(String id) {
    return getQuery(Filters.eq("_id", new ObjectId(id))).first();
  }

  public T findByField(String field, String value) {
    return getQuery(Filters.eq(field, value)).first();
  }

  public List<T> listByField(String field, String value) {
    try (final var it = getQuery(Filters.eq(field, value)).iterator()) {
      return it.toList();
    }
  }

  public List<T> listAll() {
    try (final var it = morphia.datastore().find(getEntityType()).iterator()) {
      return it.toList();
    }
  }

  public List<T> listWithFilter(Filter filter) {
    if (filter == null) {
      return List.of();
    }

    try (final var it = getQuery(filter).iterator()) {
      return it.toList();
    }
  }

  public List<T> listAllPaginated(int pageSize, int pageNum) {
    try (final var it =
        morphia
            .datastore()
            .find(getEntityType())
            .iterator(new FindOptions().skip(pageNum * pageSize).limit(pageSize))) {
      return it.toList();
    }
  }

  public List<T> listWithFilterPaginated(Filter filter, int pageSize, int pageNum) {
    if (filter == null) {
      return List.of();
    }

    try (final var it =
        getQuery(filter).iterator(new FindOptions().skip(pageNum * pageSize).limit(pageSize))) {
      return it.toList();
    }
  }

  public long count() {
    return morphia.datastore().find(getEntityType()).count();
  }

  public long count(Filter filter) {
    return getQuery(filter).count();
  }

  public void save(T entity) {
    final var dbEntity = morphia.datastore().save(entity);
    logger.debug("saved {}", dbEntity);
  }

  public void delete(T entity) {
    morphia.datastore().delete(entity);
    logger.debug("deleted {}", entity);
  }

  private Query<T> getQuery(Filter filter) {
    return morphia.datastore().find(getEntityType()).filter(filter);
  }

  @SuppressWarnings("unchecked")
  private Class<T> getEntityType() {
    final var superType = (ParameterizedType) getClass().getGenericSuperclass();
    final var superTypes = superType.getActualTypeArguments();
    return ((Class<T>) superTypes[0]);
  }
}
