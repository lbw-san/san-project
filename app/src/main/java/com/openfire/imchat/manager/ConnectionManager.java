package com.openfire.imchat.manager;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

public class ConnectionManager {


    private static XMPPTCPConnection connection;
    private static XMPPTCPConnection con;
    private static String host = "192.168.43.120";
    //private static String host = "10.157.17.65";
    private static int port = 5222;
    private static String serviceName = host;

    public static AbstractXMPPConnection getConnection() {
        if (connection == null) {
            openConnection();
        }
        return connection;
    }

    // 打开连接
    private static void openConnection() {

        XMPPTCPConnectionConfiguration.Builder builder
                = XMPPTCPConnectionConfiguration.builder();

        builder.setHost(host);
        builder.setPort(port);
        builder.setServiceName(serviceName);
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

        connection = new XMPPTCPConnection(builder.build());

        try {
            connection.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//区别于抽象连接


    // 关闭连接
    public static void release() {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }
}
