package killrvideo.utils;

import java.util.Arrays;
import java.util.StringJoiner;

public class ExceptionUtils {

    public static final String mergeStackTrace(Throwable throwable) {
        StringJoiner joiner = new StringJoiner("\n\t", "\n", "\n");
        joiner.add(throwable.getMessage());
        Arrays.asList(throwable.getStackTrace())
             .forEach(stackTraceElement -> joiner.add(stackTraceElement.toString()));

        return joiner.toString();
    }
}
