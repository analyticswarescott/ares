package com.aw.websockets;

import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@ServerEndpoint("/ws/tenants")
public class TenantNotify {
    private static final Logger logger = LoggerFactory.getLogger(TenantNotify.class);

    //queue holds the list of connected clients
    private static Queue<Session> queue = new ConcurrentLinkedQueue<Session>();

    public TenantNotify() {
    }

    public static void notify(String event) {
        try {
            ArrayList<Session> closedSessions = new ArrayList<>();
            for (Session session : queue) {
                if (!session.isOpen()) {
                    logger.info("Closed session: " + session.getId());
                    closedSessions.add(session);
                } else {
                    session.getBasicRemote().sendText(event);
                }
            }
            queue.removeAll(closedSessions);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @OnMessage
    public void onMessage(Session session, String msg) {
        try {
            logger.info("received msg " + msg + " from " + session.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void open(Session session) {
        queue.add(session);
        logger.info("New session opened: " + session.getId());
    }

    @OnError
    public void error(Session session, Throwable t) {
        queue.remove(session);
        logger.error("Error on session " + session.getId());
    }

    @OnClose
    public void closedConnection(Session session) {
        queue.remove(session);
        logger.info("session closed: " + session.getId());
    }
}
