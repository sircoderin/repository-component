package schema;

import com.github.victools.jsonschema.generator.FieldScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.generator.TypeScope;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import com.github.victools.jsonschema.module.javax.validation.JavaxValidationModule;
import com.github.victools.jsonschema.module.javax.validation.JavaxValidationOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.MatchesPattern;
import javax.annotation.Nonnull;
import models.BaseEntity;

public class JsonComponentSchemaGeneratorConfigBuilder {
  private final SchemaGeneratorConfigBuilder schemaGeneratorConfigBuilder;
  private boolean withConstraints = false;

  public JsonComponentSchemaGeneratorConfigBuilder() {
    this.schemaGeneratorConfigBuilder = getDefaultBuilder();
  }

  private static String getFieldPattern(FieldScope field) {
    final MatchesPattern matchesPatter = field.getAnnotation(MatchesPattern.class);
    return matchesPatter != null ? matchesPatter.value() : null;
  }

  public JsonComponentSchemaGeneratorConfigBuilder withConstraints() {
    schemaGeneratorConfigBuilder.with(Option.DEFINITIONS_FOR_ALL_OBJECTS);
    this.withConstraints = true;
    return this;
  }

  public JsonComponentSchemaGeneratorConfigBuilder withInline() {
    schemaGeneratorConfigBuilder.with(Option.INLINE_ALL_SCHEMAS);
    return this;
  }

  public JsonComponentSchemaGeneratorConfigBuilder withModule(Module module) {
    schemaGeneratorConfigBuilder.with(module);
    return this;
  }

  public JsonComponentSchemaGeneratorConfigBuilder withOption(Option option) {
    schemaGeneratorConfigBuilder.with(option);
    return this;
  }

  public JsonComponentSchemaGeneratorConfigBuilder withoutOption(Option option) {
    schemaGeneratorConfigBuilder.without(option);
    return this;
  }

  public SchemaGeneratorConfig build() {
    setConstraints();
    return schemaGeneratorConfigBuilder.build();
  }

  private SchemaGeneratorConfigBuilder getDefaultBuilder() {
    var configBuilder =
        new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7, OptionPreset.PLAIN_JSON)
            .with(
                new JacksonModule(
                    JacksonOption.IGNORE_TYPE_INFO_TRANSFORM,
                    JacksonOption.FLATTENED_ENUMS_FROM_JSONVALUE))
            .with(
                new JavaxValidationModule(
                    JavaxValidationOption.INCLUDE_PATTERN_EXPRESSIONS,
                    JavaxValidationOption.NOT_NULLABLE_FIELD_IS_REQUIRED));
    configBuilder
        .forFields()
        .withIgnoreCheck(
            field -> BaseEntity.class.equals(field.getDeclaringType().getErasedType()));
    return configBuilder;
  }

  private void setConstraints() {
    if (withConstraints) {
      schemaGeneratorConfigBuilder
          .forTypesInGeneral()
          .withEnumResolver(
              scope -> scope.getType().getErasedType().isEnum() ? getScopeEnumList(scope) : null);

      schemaGeneratorConfigBuilder
          .forFields()
          .withNullableCheck(field -> field.getAnnotation(Nonnull.class) == null)
          .withRequiredCheck(this::fieldRequiresCheck)
          .withStringPatternResolver(JsonComponentSchemaGeneratorConfigBuilder::getFieldPattern);
    } else {
      schemaGeneratorConfigBuilder.forFields().withNullableCheck(field -> true);
    }
  }

  private List<String> getScopeEnumList(TypeScope scope) {
    return Stream.of(scope.getType().getErasedType().getEnumConstants())
        .map(v -> ((Enum<?>) v).name())
        .collect(Collectors.toList());
  }

  private boolean fieldRequiresCheck(FieldScope field) {
    return field.getAnnotation(Nonnull.class) != null;
  }
}
