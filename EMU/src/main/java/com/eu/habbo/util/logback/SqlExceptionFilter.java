package com.eu.habbo.util.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import java.sql.SQLException;

public class SqlExceptionFilter extends Filter<ILoggingEvent> {
   public FilterReply decide(ILoggingEvent event) {
      ThrowableProxy proxy = (ThrowableProxy)event.getThrowableProxy();
      return proxy.getThrowable() instanceof SQLException ? FilterReply.ACCEPT : FilterReply.DENY;
   }
}
