package dot.cpp.repository.models;

import org.bson.conversions.Bson;

public class Index {

  public final String collectionName;
  public final String name;
  public final Bson spec;
  public final boolean unique;

  public Index(String collectionName, String name, Bson spec, boolean unique) {
    this.collectionName = collectionName;
    this.name = name;
    this.spec = spec;
    this.unique = unique;
  }

  public static Index index(String collectionName, String indexName, Bson spec, boolean unique) {
    return new Index(collectionName, indexName, spec, unique);
  }

  public static Index index(Class<?> clazz, String indexName, Bson spec, boolean unique) {
    return new Index(clazz.getSimpleName(), indexName, spec, unique);
  }
}
