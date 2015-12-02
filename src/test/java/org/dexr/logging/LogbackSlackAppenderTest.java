package org.dexr.logging;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.status.Status;
import ch.qos.logback.core.status.StatusManager;
import lombok.Setter;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class LogbackSlackAppenderTest {
    private static final String SLACK_LOGGING = "slack logging response: ";

    @Rule
    public LogbackCaptureRule logCapture = new LogbackCaptureRule();

    private LogbackSlackAppenderStub appender;

    @Mock
    private Context context;
    @Mock
    private StatusManager sm;
    @Mock
    private Layout<ILoggingEvent> layout;
    @Mock
    private ILoggingEvent event;

    @Before
    public void setMocksAndExpectations() {
        logCapture.setLogThreshold(Level.DEBUG);
        MockitoAnnotations.initMocks(this);
        when(context.getStatusManager()).thenReturn(sm);
        appender = new LogbackSlackAppenderStub();
        appender.setContext(context);
    }

    @Test
    public void one_error_status_should_be_reported_when_level_not_set_on_start_up_and_appender_reports_as_not_started() {
        appender.setEndpoint("");
        appender.setLayout(layout);
        assertThat(appender.getLevel(), is(nullValue()));
        appender.start();

        verify(sm, times(1)).add(any(Status.class));
        assertFalse(appender.isStarted());
    }

    @Test
    public void one_error_status_should_be_reported_when_endpoint_not_set_on_start_up_and_appender_reports_as_not_started() {
        appender.setLevel(Level.ERROR);
        appender.setLayout(layout);
        assertThat(appender.getEndpoint(), is(nullValue()));

        appender.start();

        verify(sm, times(1)).add(any(Status.class));
        assertFalse(appender.isStarted());
    }

    @Test
    public void one_error_status_should_be_reported_when_layout_not_set_on_start_up_and_appender_reports_as_not_started() {
        appender.setLevel(Level.ERROR);
        appender.setEndpoint("");
        assertThat(appender.getLayout(), is(nullValue()));

        appender.start();

        verify(sm, times(1)).add(any(Status.class));
        assertFalse(appender.isStarted());
    }

    @Test
    public void three_error_status_should_be_reported_when_endpoint_and_level_not_set_on_start_up_and_appender_reports_as_not_started() {
        assertThat(appender.getLevel(), is(nullValue()));
        assertThat(appender.getEndpoint(), is(nullValue()));
        assertThat(appender.getLayout(), is(nullValue()));

        appender.start();

        verify(sm, times(3)).add(any(Status.class));
        assertFalse(appender.isStarted());
    }

    @Test
    public void no_status_should_be_reported_when_endpoint_and_level_set_on_start_up_and_appender_reports_as_started() {
        appender.setLevel(Level.ERROR);
        appender.setEndpoint("");
        appender.setLayout(layout);
        assertThat(appender.getLevel(), is(org.hamcrest.Matchers.any(Level.class)));
        assertThat(appender.getEndpoint(), is(org.hamcrest.Matchers.any(String.class)));

        appender.start();

        verify(sm, never()).add(any(Status.class));
        assertTrue(appender.isStarted());
    }

    @Test
    public void message_is_sent_to_endpoint_when_event_level_matches_set_level_and_appender_is_started() {
        when(event.getLevel()).thenReturn(Level.ERROR);

        appender.setLevel(Level.ERROR);
        appender.setEndpoint("https://www.test");
        appender.setLayout(layout);
        appender.setResponse(new ResponseEntity<>(HttpStatus.OK));

        appender.start();
        appender.append(event);

        assertTrue(appender.isStarted());
        logCapture.expectedInLogEntries(Level.DEBUG, "slack logging response: <200 OK,{}>");
    }

    @Test
    public void message_is_not_sent_to_endpoint_when_appender_is_not_started() {
        when(event.getLevel()).thenReturn(Level.ERROR);

        appender.setLevel(Level.ERROR);
        appender.setEndpoint("https://www.test");
        appender.setLayout(layout);
        appender.setResponse(new ResponseEntity<>(HttpStatus.OK));

        appender.append(event);

        assertFalse(appender.isStarted());
        logCapture.noLogEntries(Level.DEBUG);
    }

    @Test
    public void message_is_not_sent_to_endpoint_when_appender_is_started_but_log_event_contains_slack_response() {
        when(event.getLevel()).thenReturn(Level.ERROR);
        when(event.getMessage()).thenReturn(SLACK_LOGGING);
        appender.setLevel(Level.ERROR);
        appender.setEndpoint("https://www.test");
        appender.setLayout(layout);
        appender.setResponse(new ResponseEntity<>(HttpStatus.OK));

        appender.start();
        appender.append(event);

        assertTrue(appender.isStarted());
        logCapture.noLogEntries(Level.DEBUG);
    }

    @Test
    public void message_is_not_sent_to_endpoint_when_appender_is_started_but_log_event_level_is_below_threshold() {
        when(event.getLevel()).thenReturn(Level.DEBUG);
        appender.setLevel(Level.ERROR);
        appender.setEndpoint("https://www.test");
        appender.setLayout(layout);
        appender.setResponse(new ResponseEntity<>(HttpStatus.OK));

        appender.start();
        appender.append(event);

        assertTrue(appender.isStarted());
        logCapture.noLogEntries(Level.DEBUG);
    }

    class LogbackSlackAppenderStub extends LogbackSlackAppender {

        @Setter
        protected ResponseEntity<String> response;

        @Override
        protected ResponseEntity<String> postMessage(Message message, RestTemplate rt) {
            return response;
        };
    }
}
