package com.od.jtimeseries.net.udp;

import com.od.jtimeseries.net.udp.message.*;
import com.od.jtimeseries.net.udp.message.javaio.JavaIOMessageFactory;
import com.od.jtimeseries.timeseries.Item;
import junit.framework.TestCase;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;

import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 24/03/12
 * Time: 16:49
 */
public class TestUdpPublicationAndSubscription extends TestCase {

    private Synchroniser synchroniser = new Synchroniser();
    private Mockery mockery = new JUnit4Mockery() {{
        setThreadingPolicy(synchroniser);
    }};

    private UdpMessageFactory udpMessageFactory = new JavaIOMessageFactory();
    private UdpServer server;
    private UdpClient client;
    private UdpServer.UdpMessageListener mockListener;
    private States sendMessageState;

    private static AtomicInteger serverPort = new AtomicInteger(25015);

    public void setUp() throws UnknownHostException {
        int port = serverPort.addAndGet(1);
        server = new UdpServer(port);
        server.startReceive();
        client = new UdpClient(new UdpClientConfig("localhost", port));
        mockListener = mockery.mock(UdpServer.UdpMessageListener.class);
        sendMessageState = mockery.states("sending");
        server.addUdpMessageListener(mockListener);
    }

    public void tearDown() {
        server.stop();
        server = null;
        client.stop();
        client = null;
        mockListener = null;
        sendMessageState = null;
    }

    public void testSeriesDescriptionMessage() throws InterruptedException {
        final SeriesDescriptionMessage m = udpMessageFactory.createTimeSeriesDescriptionMessage("test.path", "My Description");
        sendAndCheckReceived(m);
    }

    public void testTimeSeriesValueMessage() throws InterruptedException {
        final TimeSeriesValueMessage m = udpMessageFactory.createTimeSeriesValueMessage("test.path", new Item(123456, 1.23456));
        sendAndCheckReceived(m);
    }

    public void testHttpServerAnnouncementMessage() throws InterruptedException {
        final HttpServerAnnouncementMessage m = udpMessageFactory.createHttpServerAnnouncementMessage(123456, "Test Server");
        sendAndCheckReceived(m);
    }

    public void testClientAnnouncementMessage() throws InterruptedException {
        final ClientAnnouncementMessage m = udpMessageFactory.createClientAnnouncementMessage(123456, "Test Client");
        sendAndCheckReceived(m);
    }

    public void testSendOneHundredMessages() throws InterruptedException {
        List<UdpMessage> l = new LinkedList<UdpMessage>();
        for (int loop=0; loop < 100; loop ++) {
            l.add(udpMessageFactory.createClientAnnouncementMessage(loop, "Test Client"));
        }
        sendAndCheckReceived(l);
    }

    private void sendAndCheckReceived(final UdpMessage m) throws InterruptedException {
        mockery.checking(new Expectations() {{
            oneOf(mockListener).udpMessageReceived(with(equal(m)));
            then(sendMessageState.is("finished"));
        }});

        client.sendMessage(m);
        synchroniser.waitUntil(sendMessageState.is("finished"));
    }

    private void sendAndCheckReceived(final List<UdpMessage> l) throws InterruptedException {
        mockery.checking(new Expectations() {{
            for ( UdpMessage m : l) {
                oneOf(mockListener).udpMessageReceived(with(equal(m)));
            }
            then(sendMessageState.is("finished"));
        }});

        LinkedBlockingQueue queue = new LinkedBlockingQueue();
        queue.addAll(l);

        while(queue.size() > 0) {
            client.sendMessages(queue);
        }
        synchroniser.waitUntil(sendMessageState.is("finished"));
    }
}
