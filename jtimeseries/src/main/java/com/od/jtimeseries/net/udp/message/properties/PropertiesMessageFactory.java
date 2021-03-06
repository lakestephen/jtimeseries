package com.od.jtimeseries.net.udp.message.properties;

import com.od.jtimeseries.net.udp.message.*;
import com.od.jtimeseries.timeseries.TimeSeriesItem;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 14/03/12
 * Time: 18:32
 *
 * Message factory with support for parsing message properties xml using regular expressions
 * which is more efficient than the built in Properties xml decoder
 */
public class PropertiesMessageFactory implements UdpMessageFactory {

    private Pattern pattern = Pattern.compile("key=\"(\\w*?)\">([^<]*?)</entry>");
    private String lastMessageType;

    private ThreadLocal<Properties> threadLocalProperties = new ThreadLocal<Properties>() {
        public Properties initialValue() {
            return new Properties();
        }
    };

    public UdpMessage getMessage(String propertiesXml) throws IOException {
        Properties p = threadLocalProperties.get();
        UdpMessage result = null;
        try {
            Matcher m = pattern.matcher(propertiesXml);
            while ( m.find() ) {
                p.put(m.group(1), m.group(2));
            }
            result = getMessage(p);
        } finally {
            p.clear();
        }
        return result;
    }

    public String getLastMessageType() {
        return lastMessageType;
    }

    public UdpMessage getMessage(Properties p) throws IOException {
        String messageType = p.getProperty(AbstractPropertiesUdpMessage.MESSAGE_TYPE_PROPERTY);
        lastMessageType = messageType;
        UdpMessage result = null;
        if ( messageType != null) {
            if ( messageType.equals(PropertiesHttpServerAnnouncementMessage.MESSAGE_TYPE)) {
                result = new PropertiesHttpServerAnnouncementMessage(p);
            } else if ( messageType.equals(PropertiesTimeSeriesValueMessage.MESSAGE_TYPE)) {
                result = new PropertiesTimeSeriesValueMessage(p);
            } else if ( messageType.equals(PropertiesClientAnnouncementMessage.MESSAGE_TYPE)) {
                result = new PropertiesClientAnnouncementMessage(p);
            } else if ( messageType.equals(PropertiesDescriptionMessage.MESSAGE_TYPE)) {
                result = new PropertiesDescriptionMessage(p);
            } else {
                throw new IOException("Unrecognized message type " + messageType + " for PropertiesUdpMessage");
            }
        } else {
            throw new IOException("Could not determine message type for PropertiesUdpMessage");
        }
        return result;
    }

    public TimeSeriesValueMessage createTimeSeriesValueMessage(String path, TimeSeriesItem timeSeriesItem) {
        return new PropertiesTimeSeriesValueMessage(path, timeSeriesItem);
    }

    public SeriesDescriptionMessage createTimeSeriesDescriptionMessage(String path, String description) {
        return new PropertiesDescriptionMessage(path, description);
    }

    public HttpServerAnnouncementMessage createHttpServerAnnouncementMessage(int httpdPort, String description) {
        return new PropertiesHttpServerAnnouncementMessage(httpdPort, description);
    }

    public ClientAnnouncementMessage createClientAnnouncementMessage(int port, String description) {
        return new PropertiesClientAnnouncementMessage(port, description);
    }

    public List<UdpMessage> deserializeFromDatagram(byte[] buffer, int length) throws IOException {
        String s = new String(buffer, 0, length, "UTF-8");
        return Collections.singletonList(getMessage(s));
    }

    public String toString() {
        return getClass().getSimpleName() + System.identityHashCode(this);
    }
}
