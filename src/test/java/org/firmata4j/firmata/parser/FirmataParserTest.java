package org.firmata4j.firmata.parser;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.firmata4j.fsm.Event;
import org.firmata4j.fsm.FiniteStateMachine;
import org.junit.After;
import org.junit.Test;

public class FirmataParserTest {
    @After
    public void tearDown() throws InterruptedException {
        assertIfThreadStillRunning("firmata");
        assertIfThreadStillRunning("parser");
    }

    @Test
    public void testConstruct() {
        FiniteStateMachine fsm = new FiniteStateMachine();
        FirmataParser parser = new FirmataParser(fsm);
        parser.start();
        parser.stop();
    }

    @Test
    public void testStartAndStopTwice() {
        FiniteStateMachine fsm = new FiniteStateMachine();
        FirmataParser parser = new FirmataParser(fsm);
        parser.start();
        parser.start();
        parser.stop();
        parser.stop();
    }

    @Test
    public void testParse() throws InterruptedException {
        final AtomicInteger eventCount = new AtomicInteger(0);
        FiniteStateMachine fsm = new FiniteStateMachine() {
            @Override
            public void handle(Event event) {
                eventCount.incrementAndGet();
            }
        };
        FirmataParser parser = new FirmataParser(fsm);
        parser.start();
        try {
            parser.parse(new byte[]{1, 2, 3});
            int i = 0;
            while (eventCount.get() < 3 && i < 20) {
                Thread.sleep(100);
                i++;
            }
            assertEquals(
                    "Should receive 3 events for each byte",
                    3,
                    eventCount.get()
            );
        } finally {
            parser.stop();
        }
    }

    @Test
    public void testParseNull() throws InterruptedException {
        final AtomicInteger eventCount = new AtomicInteger(0);
        FiniteStateMachine fsm = new FiniteStateMachine() {
            @Override
            public void handle(Event event) {
                eventCount.incrementAndGet();
            }
        };
        FirmataParser parser = new FirmataParser(fsm);
        parser.start();
        try {
            parser.parse(null);
            int i = 0;
            while (eventCount.get() == 0 && i < 10) {
                Thread.sleep(100);
                i++;
            }
            assertEquals(
                    "Should receive no events at all with null bytes array",
                    0,
                    eventCount.get()
            );
        } finally {
            parser.stop();
        }
    }

    private static void assertIfThreadStillRunning(final String contains) throws InterruptedException {
        int count = Thread.currentThread().getThreadGroup().activeCount();
        Thread[] threads = new Thread[count];
        Thread.currentThread().getThreadGroup().enumerate(threads);
        for (Thread t : threads) {
            if (t != null && t.getName().contains(contains)) {
                t.join();
            }
        }
    }
}
