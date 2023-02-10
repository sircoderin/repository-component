package dot.cpp.repository.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;
import play.libs.Json;

public abstract class BaseEntity {

  @Id protected ObjectId id;

  /** Date and time of creation as UNIX timestamp. */
  protected Long createdAt;

  /** Id of the user who created this record. */
  protected String createdBy;

  /** Date and time of the last modification as UNIX timestamp. */
  protected Long modifiedAt;

  /** Id of the user who modified this record. */
  protected String modifiedBy;

  public ObjectId getId() {
    return id;
  }

  @JsonProperty("strId")
  public String getStrId() {
    return id != null ? id.toString() : "";
  }

  public BaseEntity setId(ObjectId id) {
    this.id = id;
    return this;
  }

  public Long getCreatedAt() {
    return createdAt;
  }

  public BaseEntity setCreatedAt(Long createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public BaseEntity setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
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

  public boolean isNew() {
    return id == null;
  }

  @Override
  public String toString() {
    return Json.stringify(Json.toJson(this));
  }
}
