package killrvideo.events;


import com.google.protobuf.GeneratedMessage;

import killrvideo.utils.ExceptionUtils;

public class CassandraMutationError {

    public final GeneratedMessage request;
    public final Throwable throwable;

    public CassandraMutationError(GeneratedMessage request, Throwable throwable) {
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
