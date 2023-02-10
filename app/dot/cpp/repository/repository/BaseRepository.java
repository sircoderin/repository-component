package dot.cpp.repository.repository;

import dev.morphia.aggregation.experimental.Aggregation;
import dev.morphia.aggregation.experimental.expressions.AccumulatorExpressions;
import dev.morphia.aggregation.experimental.expressions.Expressions;
import dev.morphia.aggregation.experimental.stages.Group;
import dev.morphia.query.FindOptions;
import dev.morphia.query.MorphiaCursor;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.experimental.filters.Filter;
import dev.morphia.query.experimental.filters.Filters;
import dot.cpp.repository.models.BaseEntity;
import dot.cpp.repository.mongodb.MorphiaService;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseRepository<T extends BaseEntity> {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Inject private MorphiaService morphia;

  public T findById(ObjectId id) {
    return getFindQuery(Filters.eq("_id", id)).first();
  }

  public T findById(String id) {
    return findById(new ObjectId(id));
  }

  public T findByField(String field, String value) {
    return getFindQuery(Filters.eq(field, value)).first();
  }

  public List<T> listByField(String field, String value, Sort... sortBy) {
    try (final var it = getFindQuery(Filters.eq(field, value)).iterator(getSortOptions(sortBy))) {
      return it.toList();
    }
  }

  public List<T> listAll(Sort... sortBy) {
    try (final var it = getFindQuery().iterator(getSortOptions(sortBy))) {
      return it.toList();
    }
  }

  public List<T> listWithFilter(Filter filter, Sort... sortBy) {
    if (filter == null) {
      return List.of();
    }

    try (final var it = getFindQuery(filter).iterator(getSortOptions(sortBy))) {
      return it.toList();
    }
  }

  public List<T> listAllPaginated(int pageSize, int pageNum, Sort... sortBy) {
    try (final var it = getFindQuery().iterator(getOptions(pageSize, pageNum).sort(sortBy))) {
      return it.toList();
    }
  }

  public List<T> listWithFilterPaginated(Filter filter, int pageSize, int pageNum, Sort... sortBy) {
    if (filter == null) {
      return List.of();
    }

    try (final var it = getFindQuery(filter).iterator(getOptions(pageSize, pageNum).sort(sortBy))) {
      return it.toList();
    }
  }

  @NotNull
  protected static FindOptions getSortOptions(Sort[] sortBy) {
    return new FindOptions().sort(sortBy);
  }

  @NotNull
  protected static FindOptions getOptions(int pageSize, int pageNum) {
    return new FindOptions().skip(pageNum * pageSize).limit(pageSize);
  }

  public long count() {
    return getFindQuery().count();
  }

  public long count(Filter filter) {
    return getFindQuery(filter).count();
  }

  public long sum(String field) {
    try (final var it = getAggregation().group(getSumGroup(field)).execute(HashMap.class)) {
      return getSumResult(it);
    }
  }

  public long sum(String field, Filter filter) {
    try (final var it = getAggregation(filter).group(getSumGroup(field)).execute(HashMap.class)) {
      return getSumResult(it);
    }
  }

  protected long getSumResult(MorphiaCursor<HashMap> it) {
    return it.hasNext() ? ((Number) it.next().get("sum")).longValue() : 0;
  }

  @NotNull
  protected Group getSumGroup(String field) {
    return Group.group().field("sum", AccumulatorExpressions.sum(Expressions.field(field)));
  }

  public T getFirstSorted(Sort sort) {
    try (final var it = getFindQuery().iterator(new FindOptions().sort(sort).limit(1))) {
      return it.tryNext();
    }
  }

  public void save(T entity) {
    final var dbEntity = morphia.datastore().save(entity);
    logger.debug("saved {}", dbEntity);
  }

  public void delete(T entity) {
    morphia.datastore().delete(entity);
    logger.debug("deleted {}", entity);
  }

  @NotNull
  protected Query<T> getFindQuery() {
    return morphia.datastore().find(getEntityType());
  }

  @NotNull
  protected Query<T> getFindQuery(Filter filter) {
    return getFindQuery().filter(filter);
  }

  @NotNull
  protected Aggregation<T> getAggregation() {
    return morphia.datastore().aggregate(getEntityType());
  }

  @NotNull
  protected Aggregation<T> getAggregation(Filter filter) {
    return getAggregation().match(filter);
  }

  @SuppressWarnings("unchecked")
  protected Class<T> getEntityType() {
    final var superType = (ParameterizedType) getClass().getGenericSuperclass();
    final var superTypes = superType.getActualTypeArguments();
    return ((Class<T>) superTypes[0]);
  }
}
