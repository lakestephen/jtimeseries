package com.od.jtimeseries.net.udp.message.javaio;

import com.od.jtimeseries.net.udp.message.MessageType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 23/03/12
 * Time: 17:57
 */
public abstract class JavaIOAnnouncementMessage extends AbstractJavaIOMessage {

    private int port;
    private String description;

    protected JavaIOAnnouncementMessage(int port, String description) {
        this.port = port;
        this.description = description;
    }

    protected JavaIOAnnouncementMessage() {}

    public int getPort() {
        return port;
    }

    public String getDescription() {
        return description;
    }

    protected void doSerializeMessageBody(DataOutputStream bos) throws IOException {
        bos.writeInt(port);
        bos.writeUTF(description);
    }

    protected void deserialize(DataInputStream is) throws IOException {
        port = is.readInt();
        description = is.readUTF();
    }
}
