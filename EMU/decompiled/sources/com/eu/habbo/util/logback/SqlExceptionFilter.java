package com.eu.habbo.util.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/util/logback/SqlExceptionFilter.class */
public class SqlExceptionFilter extends Filter<ILoggingEvent> {
    public FilterReply decide(ILoggingEvent iLoggingEvent) {
        return iLoggingEvent.getThrowableProxy().getThrowable() instanceof SQLException ? FilterReply.ACCEPT : FilterReply.DENY;
    }
}
