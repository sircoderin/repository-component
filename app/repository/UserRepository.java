package repository;

import it.unifi.cerm.playmorphia.PlayMorphia;
import javax.inject.Inject;
import models.User;

public class UserRepository {

  private final PlayMorphia morphia;

  @Inject
  public UserRepository(PlayMorphia morphia) {
    this.morphia = morphia;
  }

  public User findById(String id) {
    return morphia.datastore().createQuery(User.class).field("_id").equal(id).first();
  }

  public void save(User user) {
    morphia.datastore().save(user);
  }
}
