package dot.cpp.repository.repository;

import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import dot.cpp.repository.models.BaseEntity;
import dot.cpp.repository.mongodb.MorphiaService;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import javax.inject.Inject;
import org.bson.types.ObjectId;

public class BaseRepository<T extends BaseEntity> {

  @Inject private MorphiaService morphia;

  public T findById(String id) {
    return morphia
        .datastore()
        .find(getEntityType())
        .filter(Filters.eq("_id", new ObjectId(id)))
        .first();
  }

  public T findByField(String field, String value) {
    return morphia.datastore().find(getEntityType()).filter(Filters.eq(field, value)).first();
  }

  public List<T> listByField(String field, String value) {
    return morphia
        .datastore()
        .find(getEntityType())
        .filter(Filters.eq(field, value))
        .iterator()
        .toList();
  }

  public List<T> listAll() {
    return morphia.datastore().find(getEntityType()).iterator().toList();
  }

  public List<T> listWithFilter(Filter filter) {
    return morphia.datastore().find(getEntityType()).filter(filter).iterator().toList();
  }

  public List<T> listAllPaginated(int pageSize, int pageNum) {
    return morphia
        .datastore()
        .find(getEntityType())
        .iterator(new FindOptions().skip(pageNum * pageSize).limit(pageSize))
        .toList();
  }

  public List<T> listWithFilterPaginated(Filter filter, int pageSize, int pageNum) {
    return morphia
        .datastore()
        .find(getEntityType())
        .filter(filter)
        .iterator(new FindOptions().skip(pageNum * pageSize).limit(pageSize))
        .toList();
  }

  public void save(T entity) {
    morphia.datastore().save(entity);
  }

  public void delete(T entity) {
    morphia.datastore().delete(entity);
  }

  private Class<T> getEntityType() {
    final var superType = (ParameterizedType) getClass().getGenericSuperclass();
    final var superTypes = superType.getActualTypeArguments();
    return ((Class<T>) superTypes[0]);
  }
}
