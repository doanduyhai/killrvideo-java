package killrvideo.events;


import com.google.protobuf.GeneratedMessageV3;

import killrvideo.utils.ExceptionUtils;

public class CassandraMutationError {

    public final GeneratedMessageV3 request;
    public final Throwable throwable;

    public CassandraMutationError(GeneratedMessageV3 request, Throwable throwable) {
        this.request = request;
        this.throwable = throwable;
    }

    public String buildErrorLog() {
        StringBuilder builder = new StringBuilder();
        builder.append(request.toString()).append("\n");
        builder.append(throwable.getMessage()).append("\n");
        builder.append(ExceptionUtils.mergeStackTrace(throwable));
        return builder.toString();
    }
}
