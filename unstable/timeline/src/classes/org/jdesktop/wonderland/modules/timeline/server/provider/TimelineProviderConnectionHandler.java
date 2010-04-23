/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.server.provider;

import java.io.Serializable;
import java.util.Properties;
import org.jdesktop.wonderland.common.comms.ConnectionType;
import org.jdesktop.wonderland.common.messages.Message;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineProviderConnectionType;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQueryID;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderAddQueryRequestMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderObjectsMessage;
import org.jdesktop.wonderland.modules.timeline.common.provider.messages.ProviderResetResultMessage;
import org.jdesktop.wonderland.modules.timeline.server.provider.TimelineProviderRegistry.RegistryResultListener;
import org.jdesktop.wonderland.server.comms.ClientConnectionHandler;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import org.jdesktop.wonderland.server.comms.annotation.ClientHandler;

/**
 * Connection handler for the TimelineProviderConnection.  This handles messages
 * from the provider application.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@ClientHandler
public class TimelineProviderConnectionHandler 
        implements ClientConnectionHandler, Serializable
{
    public ConnectionType getConnectionType() {
        return TimelineProviderConnectionType.TYPE;
    }

    public void registered(WonderlandClientSender sender) {
    }

    public void clientConnected(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                Properties properties)
    {
        TimelineProviderRegistry registry = TimelineProviderRegistry.getInstance();

        // a new client is connected. Reset any existing results.
        for (TimelineQueryID id : registry.getRegisteredQueries()) {
            RegistryResultListener listener = registry.getListener(id);
            listener.reset();
        }

        // Send the new client all of the outstanding queries
        for (TimelineQueryID id : registry.getRegisteredQueries()) {
            TimelineQuery query = registry.getQuery(id);
            sender.send(clientID, new ProviderAddQueryRequestMessage(query));
        }
    }

    public void messageReceived(WonderlandClientSender sender,
                                WonderlandClientID clientID,
                                Message message)
    {
        if (message instanceof ProviderObjectsMessage) {
            handleObjects((ProviderObjectsMessage) message);
        } else if (message instanceof ProviderResetResultMessage) {
            handleResetResults((ProviderResetResultMessage) message);
        }
    }

    private void handleObjects(ProviderObjectsMessage message) {
        TimelineProviderRegistry registry = TimelineProviderRegistry.getInstance();

        RegistryResultListener listener = registry.getListener(message.getID());
        switch (message.getAction()) {
            case ADD:
                listener.added(message.getObjects());
                break;
            case REMOVE:
                listener.removed(message.getObjects());
                break;
        }
    }

    private void handleResetResults(ProviderResetResultMessage message) {
        TimelineProviderRegistry registry = TimelineProviderRegistry.getInstance();

        RegistryResultListener listener = registry.getListener(message.getID());
        listener.reset();
    }

    public void clientDisconnected(WonderlandClientSender sender,
                                   WonderlandClientID clientID)
    {
        // do nothing -- we'll deal with resetting everything on disconnect
    }
}
