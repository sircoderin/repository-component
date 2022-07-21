package dot.cpp.repository.repository;

import dev.morphia.query.FindOptions;
import dev.morphia.query.Sort;
import dot.cpp.repository.models.BaseEntity;
import it.unifi.cerm.playmorphia.PlayMorphia;
import java.util.List;
import org.bson.types.ObjectId;

public class BaseRepository<T extends BaseEntity> {

  private final PlayMorphia morphia;

  public BaseRepository(PlayMorphia morphia) {
    this.morphia = morphia;
  }

  public T findById(Class<T> clazz, String id) {
    return morphia.datastore().createQuery(clazz).field("_id").equal(new ObjectId(id)).first();
  }

  public T findByField(Class<T> clazz, String field, String value) {
    return morphia.datastore().createQuery(clazz).field(field).equal(value).first();
  }

  public List<T> listByField(Class<T> clazz, String field, String value) {
    return morphia.datastore().createQuery(clazz).field(field).equal(value).find().toList();
  }

  public List<T> listAllPaginated(Class<T> clazz, int pageSize, int pageNum) {
    return morphia
        .datastore()
        .createQuery(clazz)
        .order(Sort.ascending("_id"))
        .find(new FindOptions().skip(pageNum * pageSize).limit(pageSize))
        .toList();
  }

  public List<T> listWithFilterPaginated(
      Class<T> clazz, String condition, String value, int pageSize, int pageNum) {
    return morphia
        .datastore()
        .createQuery(clazz)
        .filter(condition, value)
        .order(Sort.ascending("_id"))
        .find(new FindOptions().skip(pageNum * pageSize).limit(pageSize))
        .toList();
  }

  public void save(T entity) {
    morphia.datastore().save(entity);
  }

  public void delete(T entity) {
    morphia.datastore().delete(entity);
  }
}
