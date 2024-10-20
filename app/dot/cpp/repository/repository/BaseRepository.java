package dot.cpp.repository.repository;

import static dev.morphia.query.filters.Filters.and;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.in;
import static dot.cpp.repository.models.BaseEntity.RECORD_ID;
import static dot.cpp.repository.models.BaseEntity.TIMESTAMP;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import dev.morphia.DatastoreImpl;
import dev.morphia.DeleteOptions;
import dev.morphia.aggregation.Aggregation;
import dev.morphia.aggregation.AggregationImpl;
import dev.morphia.aggregation.expressions.AccumulatorExpressions;
import dev.morphia.aggregation.expressions.Expressions;
import dev.morphia.aggregation.stages.Group;
import dev.morphia.query.FindOptions;
import dev.morphia.query.MorphiaCursor;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.filters.Filter;
import dot.cpp.repository.models.BaseEntity;
import dot.cpp.repository.mongodb.MorphiaService;
import dot.cpp.repository.services.RepositoryService;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseRepository<T extends BaseEntity> {

  private static final String INITIAL = "initial";
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Inject private MorphiaService morphia;
  @Inject private RepositoryService repositoryService;

  @NotNull
  protected static FindOptions getSortOptions(Sort[] sortBy) {
    return new FindOptions().sort(sortBy);
  }

  @NotNull
  protected static FindOptions getOptions(int pageSize, int pageNum) {
    return new FindOptions().skip(pageNum * pageSize).limit(pageSize);
  }

  @NotNull
  private static FindOptions getFindOptions(int skip, int limit) {
    return new FindOptions().skip(skip).limit(limit);
  }

  public T findFirst(Filter filter) {
    return getFindQuery(filter).first();
  }

  public T findFirst(Sort sort) {
    try (final var it = getFindQuery().iterator(new FindOptions().sort(sort).limit(1))) {
      return it.tryNext();
    }
  }

  public T findFirst(Filter filter, Sort sort) {
    try (final var it = getFindQuery(filter).iterator(new FindOptions().sort(sort).limit(1))) {
      return it.tryNext();
    }
  }

  public T findById(String id) {
    return getFindQuery(eq(RECORD_ID, id)).first();
  }

  public List<T> findByIds(List<String> ids) {
    return getFindQuery(in(RECORD_ID, ids)).stream().collect(Collectors.toList());
  }

  public T findHistoryRecord(String id, Long timestamp) {
    return getHistoryCollection()
        .find(Filters.and(Filters.eq(RECORD_ID, id), Filters.eq(TIMESTAMP, timestamp)))
        .first();
  }

  public T findByField(String field, String value) {
    return getFindQuery(eq(field, value)).first();
  }

  public List<T> list(Filter filter, int skip, int limit, Sort... sortBy) {
    try (final var it = getFindQuery(filter).iterator(getFindOptions(skip, limit).sort(sortBy))) {
      return it.toList();
    }
  }

  public List<T> listAll(Sort... sortBy) {
    try (final var it = getFindQuery().iterator(getSortOptions(sortBy))) {
      return it.toList();
    }
  }

  public List<T> listByField(String field, String value, Sort... sortBy) {
    try (final var it = getFindQuery(eq(field, value)).iterator(getSortOptions(sortBy))) {
      return it.toList();
    }
  }

  public List<T> listWithFilter(Filter filter, Sort... sortBy) {
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
    try (final var it = getFindQuery(filter).iterator(getOptions(pageSize, pageNum).sort(sortBy))) {
      return it.toList();
    }
  }

  public List<T> listHistoryRecords(String id) {
    final var historyEntities = new ArrayList<T>();

    getHistoryCollection()
        .find(Filters.eq(RECORD_ID, id))
        .sort(Sorts.descending(TIMESTAMP))
        .forEach(historyEntities::add);

    return historyEntities;
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

  public T saveWithHistory(T entity) {
    final var currentEntity = findById(entity.getRecordId());

    entity.setModifiedAt(Instant.now().getEpochSecond());

    if (currentEntity == null) {
      entity.setModifiedComment(INITIAL);
    } else {
      currentEntity.setId(new ObjectId());
      getHistoryCollection().insertOne(currentEntity);
    }

    final var savedEntity = morphia.datastore().save(entity);
    logger.debug("history save {}", savedEntity);

    return savedEntity;
  }

  public List<T> saveWithHistory(List<T> entities) {
    final var ids = entities.stream().map(BaseEntity::getRecordId).collect(Collectors.toList());
    if (ids.isEmpty()) {
      return new ArrayList<>();
    }

    final var historyEntries =
        findByIds(ids).stream()
            .map(
                entity -> {
                  entity.setId(new ObjectId());
                  return entity;
                })
            .collect(Collectors.toList());

    getHistoryCollection().insertMany(historyEntries);

    entities.forEach(entity -> entity.setModifiedAt(Instant.now().getEpochSecond()));
    final var savedEntities = morphia.datastore().save(entities);
    logger.debug("history save entities {}", savedEntities);

    return savedEntities;
  }

  public T save(T entity) {
    entity.setModifiedAt(Instant.now().getEpochSecond());

    final var savedEntity = morphia.datastore().save(entity);
    logger.debug("save {}", savedEntity);

    return savedEntity;
  }

  public List<T> save(List<T> entities) {
    final var timestamp = Instant.now().getEpochSecond();
    entities.forEach(entity -> entity.setModifiedAt(timestamp));

    final var savedEntities = morphia.datastore().save(entities);
    logger.debug("save entities {}", savedEntities);

    return savedEntities;
  }

  public void delete(T entity) {
    morphia.datastore().delete(entity);
    logger.debug("deleted {}", entity);
  }

  public long deleteWithFilter(Filter filter) {
    return getFindQuery(filter).delete(new DeleteOptions().multi(true)).getDeletedCount();
  }

  @NotNull
  protected Query<T> getFindQuery() {
    return morphia.datastore().find(getEntityType());
  }

  @NotNull
  protected Query<T> getFindQuery(Filter filter) {
    return filter != null ? getFindQuery().filter(filter) : getFindQuery();
  }

  @NotNull
  protected Aggregation<T> getAggregation() {
    return morphia.datastore().aggregate(getEntityType());
  }

  @NotNull
  protected Aggregation<T> getAggregation(Filter filter) {
    return filter != null ? getAggregation().match(filter) : getAggregation();
  }

  @NotNull
  protected Aggregation<T> getAggregation(Filter filter, int skip, int limit) {
    return getAggregation(filter).skip(skip).limit(limit);
  }

  @NotNull
  protected Aggregation<T> getAggregation(
      Filter filter, dev.morphia.aggregation.stages.Sort sortBy, int skip, int limit) {
    return getAggregation(filter).sort(sortBy).skip(skip).limit(limit);
  }

  @NotNull
  protected Aggregation<T> getAggregation(Filter filter, long timestamp) {
    return timestamp != 0L
        ? getHistoryAggregation(and(filter, eq(TIMESTAMP, timestamp)))
        : getAggregation(filter);
  }

  @NotNull
  protected Aggregation<T> getAggregation(String id, long timestamp) {
    return timestamp != 0L
        ? getHistoryAggregation(and(eq(RECORD_ID, id), eq(TIMESTAMP, timestamp)))
        : getAggregation(eq(RECORD_ID, id));
  }

  @SuppressWarnings("unchecked")
  protected Class<T> getEntityType() {
    final var superType = (ParameterizedType) getClass().getGenericSuperclass();
    final var superTypes = superType.getActualTypeArguments();
    return ((Class<T>) superTypes[0]);
  }

  /**
   * Morphia sets codec registries automatically from POJOs, but the Mongo client needs manual
   * setup. History collections must be initialized using {@link RepositoryService} to support
   * indexing.
   */
  @NotNull
  protected MongoCollection<T> getHistoryCollection() {
    return morphia
        .datastore()
        .getDatabase()
        .getCollection(getEntityType().getSimpleName() + "_history", getEntityType());
  }

  @NotNull
  protected Aggregation<T> getHistoryAggregation(Filter filter) {
    final var historyAggregation =
        new AggregationImpl<>(
            (DatastoreImpl) morphia.datastore(),
            morphia
                .datastore()
                .getDatabase()
                .getCollection(getEntityType().getSimpleName() + "_history", getEntityType()));
    return filter != null ? historyAggregation.match(filter) : historyAggregation;
  }

  public void emptyCollection() {
    repositoryService.emptyCollection(getEntityType());
  }
}
