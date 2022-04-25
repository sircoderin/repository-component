package dot.com.repository.repository;

import dot.com.repository.models.User;
import it.unifi.cerm.playmorphia.PlayMorphia;
import javax.inject.Inject;

public class UserRepository {

  private final PlayMorphia morphia;

  @Inject
  public UserRepository(PlayMorphia morphia) {
    this.morphia = morphia;
  }

  public User findById(String id) {
    return morphia.datastore().createQuery(User.class).field("_id").equal(id).first();
  }

  public User findByField(String field, String value) {
    return morphia.datastore().createQuery(User.class).field(field).equal(value).first();
  }

  public void save(User user) {
    morphia.datastore().save(user);
  }
}
