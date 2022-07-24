package dot.cpp.repository.repository;

import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filters;
import dot.cpp.repository.models.BaseEntity;
import dot.cpp.repository.mongodb.MorphiaService;
import java.util.List;
import javax.inject.Inject;
import org.bson.types.ObjectId;

public class BaseRepository<T extends BaseEntity> {

  @Inject private MorphiaService morphia;

  public T findById(Class<T> clazz, String id) {
    return morphia.datastore().find(clazz).filter(Filters.eq("_id", new ObjectId(id))).first();
  }

  public T findByField(Class<T> clazz, String field, String value) {
    return morphia.datastore().find(clazz).filter(Filters.eq(field, value)).first();
  }

  public List<T> listByField(Class<T> clazz, String field, String value) {
    return morphia.datastore().find(clazz).filter(Filters.eq(field, value)).iterator().toList();
  }

  public List<T> listAll(Class<T> clazz) {
    return morphia.datastore().find(clazz).iterator().toList();
  }

  public List<T> listAllPaginated(Class<T> clazz, int pageSize, int pageNum) {
    return morphia
        .datastore()
        .find(clazz)
        .iterator(new FindOptions().skip(pageNum * pageSize).limit(pageSize))
        .toList();
  }

  public void save(T entity) {
    morphia.datastore().save(entity);
  }

  public void delete(T entity) {
    morphia.datastore().delete(entity);
  }
}
