package dot.cpp.repository.repository;

import dot.cpp.repository.models.BaseEntity;
import it.unifi.cerm.playmorphia.PlayMorphia;
import java.util.List;
import javax.inject.Inject;

public class BaseRepository<T extends BaseEntity> {

  private final PlayMorphia morphia;

  @Inject
  public BaseRepository(PlayMorphia morphia) {
    this.morphia = morphia;
  }

  public T findById(String id, Class<T> clazz) {
    return morphia.datastore().createQuery(clazz).field("_id").equal(id).first();
  }

  public List<T> findByField(String field, String value, Class<T> clazz) {
    return morphia.datastore().createQuery(clazz).field(field).equal(value).find().toList();
  }

  public void save(T user) {
    morphia.datastore().save(user);
  }
}
