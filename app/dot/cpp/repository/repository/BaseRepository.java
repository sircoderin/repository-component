package dot.cpp.repository.repository;

import dot.cpp.repository.models.BaseEntity;
import it.unifi.cerm.playmorphia.PlayMorphia;
import java.util.List;
import org.bson.types.ObjectId;

public class BaseRepository<T extends BaseEntity> {

  private final PlayMorphia morphia;

  public BaseRepository(PlayMorphia morphia) {
    this.morphia = morphia;
  }

  public T findById(String id, Class<T> clazz) {
    return morphia.datastore().createQuery(clazz).field("_id").equal(new ObjectId(id)).first();
  }

  public T findByField(String field, String value, Class<T> clazz) {
    return morphia.datastore().createQuery(clazz).field(field).equal(value).first();
  }

  public List<T> listByField(String field, String value, Class<T> clazz) {
    return morphia.datastore().createQuery(clazz).field(field).equal(value).find().toList();
  }

  public void save(T user) {
    morphia.datastore().save(user);
  }
}
