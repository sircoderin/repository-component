package dot.cpp.repository.models;

import org.bson.conversions.Bson;

public class Index {

  public final String collectionName;
  public final String name;
  public final Bson specification;
  public final boolean unique;

  public Index(String collectionName, String name, Bson specification, boolean unique) {
    this.collectionName = collectionName;
    this.name = name;
    this.specification = specification;
    this.unique = unique;
  }

  public static Index from(
      String collectionName, String indexName, Bson specification, boolean unique) {
    return new Index(collectionName, indexName, specification, unique);
  }

  public static Index from(Class<?> clazz, String indexName, Bson specification, boolean unique) {
    return new Index(clazz.getSimpleName(), indexName, specification, unique);
  }
}
