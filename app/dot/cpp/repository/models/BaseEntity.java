package dot.cpp.repository.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;
import play.libs.Json;

public abstract class BaseEntity {

  @Id protected ObjectId id;

  protected String trackingId;

  /** Date and time of the last modification as UNIX timestamp. */
  protected Long modifiedAt;

  /** Id of the user who modified this record. */
  protected String modifiedBy;

  /** Comment describing the last modification. */
  protected String modifiedComment;

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

  public String getTrackingId() {
    return trackingId;
  }

  public void setTrackingId(String trackingId) {
    this.trackingId = trackingId;
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
