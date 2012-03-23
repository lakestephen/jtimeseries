package com.od.jtimeseries.net.udp.message.javaio;

import com.od.jtimeseries.net.udp.message.HttpServerAnnouncementMessage;
import com.od.jtimeseries.net.udp.message.MessageType;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 23/03/12
 * Time: 18:00
 */
public class JavaIOHttpServerAnnouncementMessage extends JavaIOAnnouncementMessage implements HttpServerAnnouncementMessage {

    public JavaIOHttpServerAnnouncementMessage(int port, String description) {
        super(port, description);
    }

    public JavaIOHttpServerAnnouncementMessage() {
    }

    public MessageType getMessageType() {
        return MessageType.SERVER_ANNOUNCE;
    }
}
