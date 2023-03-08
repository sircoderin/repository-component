package dot.cpp.repository.repository;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

import com.mongodb.client.MongoCollection;
import dev.morphia.aggregation.Aggregation;
import dev.morphia.aggregation.expressions.AccumulatorExpressions;
import dev.morphia.aggregation.expressions.Expressions;
import dev.morphia.aggregation.stages.Group;
import dev.morphia.query.FindOptions;
import dev.morphia.query.MorphiaCursor;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.filters.Filter;
import dev.morphia.query.filters.Filters;
import dot.cpp.repository.models.BaseEntity;
import dot.cpp.repository.mongodb.MorphiaService;
import dot.cpp.repository.services.RepositoryService;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseRepository<T extends BaseEntity> {

  private static final String INITIAL = "initial";
  private final Logger logger = LoggerFactory.getLogger(getClass());
  @Inject
  private MorphiaService morphia;

  @NotNull
  protected static FindOptions getSortOptions(Sort[] sortBy) {
    return new FindOptions().sort(sortBy);
  }

  @NotNull
  protected static FindOptions getOptions(int pageSize, int pageNum) {
    return new FindOptions().skip(pageNum * pageSize).limit(pageSize);
  }

  public T findById(ObjectId id) {
    return getFindQuery(Filters.eq("_id", id)).first();
  }

  public T findById(String id) {
    return findById(new ObjectId(id));
  }

  public T findByHistoryId(String id) {
    return getHistoryCollection().find(eq("_id", new ObjectId(id))).first();
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

  public List<T> listHistory(String trackingId) {
    final var historyEntities = new ArrayList<T>();

    getHistoryCollection()
        .find(eq("trackingId", trackingId))
        .sort(descending("modifiedAt"))
        .forEach(historyEntities::add);

    return historyEntities;
  }

  /**
   * Morphia sets the codec registries automatically from the POJOs, but the Mongo client needs manual setup
   * History collections must be initialized using {@link RepositoryService} to support indexing
   */
  @NotNull
  private MongoCollection<T> getHistoryCollection() {
    return morphia
        .datastore()
        .getDatabase()
        .getCollection(getEntityType().getSimpleName() + "_history", getEntityType());
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

  public void saveWithHistory(T entity) {
    final var currentEntity = findById(entity.getId());

    entity.setModifiedAt(Instant.now().getEpochSecond());

    if (currentEntity == null) {
      entity.setTrackingId(UUID.randomUUID().toString());
      entity.setModifiedComment(INITIAL);
    } else {
      currentEntity.setId(new ObjectId());
      getHistoryCollection().insertOne(currentEntity);
    }

    final var savedEntity = morphia.datastore().save(entity);
    logger.debug("saved {}", savedEntity);
  }

  public void save(T entity) {
    final var savedEntity = morphia.datastore().save(entity);
    logger.debug("saved {}", savedEntity);
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
