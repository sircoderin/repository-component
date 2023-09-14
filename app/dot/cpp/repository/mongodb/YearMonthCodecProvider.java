package dot.cpp.repository.mongodb;

import java.time.YearMonth;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class YearMonthCodecProvider implements CodecProvider {

  @Override
  public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
    if (clazz == YearMonth.class) {
      return (Codec<T>) new YearMonthCodec();
    }
    return null;
  }
}
