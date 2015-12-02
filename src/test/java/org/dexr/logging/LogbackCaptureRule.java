package org.dexr.logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.assertThat;

import static java.util.stream.Collectors.toList;

@Getter
@Setter
public class LogbackCaptureRule implements TestRule {
    private final ArrayList<Matcher<Iterable<? super LogAssertion>>> iterableMatchers = new ArrayList<>();
    private final ArrayList<Matcher<Collection<LogAssertion>>> collectionMatchers = new ArrayList<>();

    private ListAppender<ILoggingEvent> listAppender;
    private ThresholdFilter filter;

    @Before
    public void setUp() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        setListAppender(new ListAppender<>());
        setFilter(new ThresholdFilter());
        setLogThreshold(Level.WARN);
        getFilter().start();
        getListAppender().addFilter(getFilter());
        getListAppender().start();
        root.addAppender(getListAppender());
    }

    @After
    public void tearDown() {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.detachAppender(getListAppender());
        getIterableMatchers().clear();
        getCollectionMatchers().clear();

    }

    public void setLogThreshold(Level level) {
        getFilter().setLevel(level.toString());
    }

    public void expectedInLogEntries(Level level, String string) {
        iterableMatchers.add(hasItem(new LogAssertion(level, string)));
    }

    public void noLogEntries(Level level) {
        collectionMatchers.add(emptyCollectionOf(LogAssertion.class));
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return statement(base);
    }

    private Statement statement(final Statement base) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setUp();
                try {
                    base.evaluate();
                    verifyMessages();
                } finally {
                    tearDown();
                }
            }

            public void verifyMessages() {
                getListAppender().stop();
                List<LogAssertion> caughtAssertions =
                        getListAppender().list.stream().map(e -> new LogAssertion(e.getLevel(), e.getFormattedMessage())).collect(toList());

                collectionMatchers.forEach(m -> assertThat(caughtAssertions, m));
                iterableMatchers.forEach(m -> assertThat(caughtAssertions, m));
            }
        };
    }

    @Getter
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    private static class LogAssertion {
        private final Level level;
        private final String message;
    }
}
