package dot.com.models;

import com.google.gson.Gson;
import dev.morphia.annotations.Id;

public abstract class BaseEntity {

  @Id protected String id;

  /** Stores the time of creation in format 2020-03-30 23:27:45. */
  protected Long createdAt;

  /** Id of the user who created this record. */
  protected String createdBy;

  /** The date time of the last modification in format 2020-03-30 23:27:45. */
  protected Long modifiedAt;

  /** Id of the user who modified this record. */
  protected String modifiedBy;

  public String getId() {
    return id;
  }

  public BaseEntity setId(String id) {
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

  @Override
  public String toString() {
    return new Gson().toJson(this);
  }
}
