package org.dexr.logging;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.status.ErrorStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class LogbackSlackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
    private static final String SLACK_LOGGING = "slack logging response: ";
    private static final String SLACK_LOGGING_RESPONSE = SLACK_LOGGING + "{}";
    private String endpoint;
    private Level level;
    private Layout<ILoggingEvent> layout;

    @Override
    public void start() {
        int errors = 0;
        if (level == null) {
            addStatus(new ErrorStatus("No level set for the appender named \"" + name + "\".", this));
            errors++;
        }
        if (endpoint == null) {
            addStatus(new ErrorStatus("No endpoint set for the appender named \"" + name + "\".", this));
            errors++;
        }
        if (layout == null) {
            addStatus(new ErrorStatus("No layout set for the appender named \"" + name + "\".", this));
            errors++;
        }
        if (errors == 0) {
            super.start();
        }
    };

    @Override
    protected void append(ILoggingEvent evt) {
        if (!isStarted()) {
            return;
        }

        if (!StringUtils.contains(evt.getMessage(), SLACK_LOGGING)) {
            if (evt.getLevel().isGreaterOrEqual(level)) {
                Message message = new Message(layout.doLayout(evt));
                RestTemplate rt = new RestTemplate();
                ResponseEntity<String> response = postMessage(message, rt);
                log.debug(SLACK_LOGGING_RESPONSE, response);
            }
        }
    }

    protected ResponseEntity<String> postMessage(Message message, RestTemplate rt) {
        ResponseEntity<String> response = rt.postForEntity(endpoint, message, String.class);
        return response;
    }

    @Value
    static class Message {
        private String text;
    }
}
