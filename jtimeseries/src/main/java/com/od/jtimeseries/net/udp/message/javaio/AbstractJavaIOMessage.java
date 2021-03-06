package com.od.jtimeseries.net.udp.message.javaio;

import com.od.jtimeseries.net.udp.message.Encoding;
import com.od.jtimeseries.net.udp.message.UdpMessage;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 21/03/12
 * Time: 21:03
 *
 * A binary encoded message which can be streamed
 */
public abstract class AbstractJavaIOMessage implements UdpMessage {

    private String sourceInetAddress;
    private String sourceHostname;

    public static final byte[] JAVA_IO_MESSAGE_HEADER = new byte[] { 'J', 'I', 'O' };

    protected AbstractJavaIOMessage() {
    }

    protected AbstractJavaIOMessage(String sourceHostname) {
        this.sourceHostname = sourceHostname;
    }

    public String getSourceInetAddress() {
        return sourceInetAddress;
    }

    public void setSourceInetAddress(String sourceInetAddress) {
        this.sourceInetAddress = sourceInetAddress;
    }

    public String getSourceHostname() {
        return this.sourceHostname == null ? sourceInetAddress : sourceHostname;
    }

    public void setSourceHostname(String sourceHostname) {
        this.sourceHostname = sourceHostname;
    }

    public void serialize(OutputStream outputStream) throws IOException {
        writeHeaderFields(outputStream);
        writeBody(outputStream);
    }

    private void writeBody(OutputStream outputStream) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(32);
        DataOutputStream d = new DataOutputStream(bos);
        d.writeUTF(getSourceHostname());
        doSerializeMessageBody(d);
        outputStream.write(bos.toByteArray());
    }

    protected abstract void doSerializeMessageBody(DataOutputStream bos) throws IOException;

    protected void deserialize(DataInputStream is, char acronymVersion) throws IOException {
        sourceHostname = is.readUTF();
        doDeserialize(is, acronymVersion);
    }

    /**
     * @param acronymVersion the second char from the message acronym, indicating message version
     */
    protected abstract void doDeserialize(DataInputStream is, char acronymVersion) throws IOException;

    private void writeHeaderFields(OutputStream outputStream) throws IOException {
        outputStream.write(AbstractJavaIOMessage.JAVA_IO_MESSAGE_HEADER);
        outputStream.write(getHeaderAcronym());
    }

    /**
     * @return a two byte value representing the message type and subversion
     */
    protected abstract byte[] getHeaderAcronym();

    public int getMaxExpectedSize() {
        return 64; //TODO
    }

    public Encoding getEncoding() {
        return Encoding.JAVA_IO;
    }

    public boolean isMessageStreamingSupported() {
        return true;
    }

    //source hostname and ip are not considered in equals comparison
    //this would be unhelpful for testing, since only set on server side on receipt
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || ! (o instanceof UdpMessage)) return false;
        return true;
    }

    public String toString() {
        return "sourceInetAddress='" + getSourceInetAddress() + '\'' +
                ", sourceHostname='" + getSourceHostname() + '\'';
    }
}
