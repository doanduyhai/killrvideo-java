package killrvideo.utils;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import com.google.protobuf.Timestamp;

import killrvideo.common.CommonTypes.TimeUuid;
import killrvideo.common.CommonTypes.Uuid;

public class TypeConverter {

    public static Timestamp instantToTimeStamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano()).build();
    }

    public static Timestamp dateToTimestamp(Date date) {
        return instantToTimeStamp(date.toInstant());
    }

    public static TimeUuid uuidToTimeUuid(UUID uuid) {
        return TimeUuid.newBuilder()
                .setValue(uuid.toString())
                .build();
    }

    public static Uuid uuidToUuid(UUID uuid) {
        return Uuid.newBuilder()
                .setValue(uuid.toString())
                .build();
    }
}
