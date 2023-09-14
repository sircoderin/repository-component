package dot.cpp.repository.mongodb;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class YearMonthCodec implements Codec<YearMonth> {

  public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM-yyyy");

  @Override
  public void encode(BsonWriter writer, YearMonth yearMonth, EncoderContext encoderContext) {
    writer.writeString(yearMonth.format(FORMATTER));
  }

  @Override
  public YearMonth decode(BsonReader reader, DecoderContext decoderContext) {
    final var yearMonthString = reader.readString();
    return YearMonth.parse(yearMonthString, FORMATTER);
  }

  @Override
  public Class<YearMonth> getEncoderClass() {
    return YearMonth.class;
  }
}
