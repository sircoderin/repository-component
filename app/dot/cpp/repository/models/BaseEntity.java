package dot.cpp.repository.models;

import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import java.util.UUID;
import org.bson.types.ObjectId;
import play.libs.Json;

public abstract class BaseEntity {

  public static final String RECORD_ID = "recordId";
  public static final String TIMESTAMP = "modifiedAt";

  @Id protected ObjectId id;

  @Indexed(options = @IndexOptions(unique = true))
  protected String recordId = UUID.randomUUID().toString();

  /** Date and time of the last modification as UNIX timestamp. */
  protected Long modifiedAt;

  /** Id of the user who modified this record. */
  protected String modifiedBy;

  /** Comment describing the last modification. */
  protected String modifiedComment;

  public ObjectId getId() {
    return id;
  }

  public BaseEntity setId(ObjectId id) {
    this.id = id;
    return this;
  }

  public String getRecordId() {
    return recordId;
  }

  public void setRecordId(String recordId) {
    this.recordId = recordId;
  }

  public Long getModifiedAt() {
    return modifiedAt;
  }

  public BaseEntity setModifiedAt(Long modifiedAt) {
    this.modifiedAt = modifiedAt;
    return this;
  }

  public String getModifiedBy() {
    return modifiedBy;
  }

  public BaseEntity setModifiedBy(String modifiedBy) {
    this.modifiedBy = modifiedBy;
    return this;
  }

  public String getModifiedComment() {
    return modifiedComment;
  }

  public void setModifiedComment(String modifiedComment) {
    this.modifiedComment = modifiedComment;
  }

  public boolean isNew() {
    return id == null;
  }

  @Override
  public String toString() {
    return Json.stringify(Json.toJson(this));
  }
}
