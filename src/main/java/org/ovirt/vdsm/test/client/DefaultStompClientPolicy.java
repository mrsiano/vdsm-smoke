package org.ovirt.vdsm.test.client;

import java.io.IOException;

import org.ovirt.vdsm.jsonrpc.client.reactors.stomp.StompClientPolicy;

public class DefaultStompClientPolicy extends StompClientPolicy {

    public DefaultStompClientPolicy() {
        super(180000, 0, 10000, IOException.class, "jms.topic.vdsm_requests", "jms.queue.smoke");
    }
}
