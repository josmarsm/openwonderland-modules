/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package org.jdesktop.wonderland.modules.cmu.common.messages.servercmu;

import java.util.ArrayList;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.messages.MessageID;
import org.jdesktop.wonderland.common.messages.ResponseMessage;
import org.jdesktop.wonderland.modules.cmu.common.events.EventResponseList;
import org.jdesktop.wonderland.modules.cmu.common.events.responses.CMUResponseFunction;

/**
 * Response message sent by a CMU program manager to a CMUCellMO after a
 * requested program has been created, containing information about how
 * to connect to the created program (or, if the connection was not
 * successfully set up, tells us so).
 * @author kevin
 */
public class CreateProgramResponseMessage extends ResponseMessage {

    private static final long serialVersionUID = 1L;
    private String hostname = null;
    private int port = 0;
    private ArrayList<CMUResponseFunction> allowedResponses = null;
    private boolean creationSuccessful = false;
    private CellID cellID = null;
    private EventResponseList initialEventList = null;

    /**
     * Basic constructor.  Initializes the message as representing unsuccessful
     * program creation.
     * @param messageID The message ID of the CreateProgramMessage that this is in response to
     * @param cellID The relevant cell ID
     */
    public CreateProgramResponseMessage(MessageID messageID, CellID cellID) {
        super(messageID);
        this.setCellID(cellID);
    }

    /**
     * Constructor with connection information.  Initializes the message as
     * representing successful program creation.
     * @param messageID The message ID of the CreateProgramMessage that this is in response to
     * @param cellID The relevant cell ID
     * @param hostname The host to connect to
     * @param port The port to connect to
     */
    public CreateProgramResponseMessage(MessageID messageID, CellID cellID, String hostname, int port,
            ArrayList<CMUResponseFunction> allowedResponses, EventResponseList initialEventList) {
        this(messageID, cellID);
        this.setHostnameAndPort(hostname, port);
        this.setAllowedResponses(allowedResponses);
        this.setInitialEventList(initialEventList);
    }

    /**
     * Get the ID of the relevant cell.
     * @return Current cell ID
     */
    public CellID getCellID() {
        return cellID;
    }

    /**
     * Set the ID of the relevant cell.
     * @param cellID New cell ID
     */
    public void setCellID(CellID cellID) {
        this.cellID = cellID;
    }

    /**
     * Get the port to connect to.
     * @return Current port
     */
    public int getPort() {
        assert this.isCreationSuccessful();
        return port;
    }

    /**
     * Get the host address to connect to.
     * @return Current host address
     */
    public String getHostname() {
        assert this.isCreationSuccessful();
        return hostname;
    }

    /**
     * Set the server and port to connect to.  Marks the message as one of
     * successful program creation.
     * @param server New host address
     * @param port New port
     */
    public void setHostnameAndPort(String server, int port) {
        this.hostname = server;
        this.port = port;
        this.creationSuccessful = true;
    }

    public ArrayList<CMUResponseFunction> getAllowedResponses() {
        return allowedResponses;
    }

    public void setAllowedResponses(ArrayList<CMUResponseFunction> allowedResponses) {
        this.allowedResponses = allowedResponses;
    }

    public EventResponseList getInitialEventList() {
        return initialEventList;
    }

    public void setInitialEventList(EventResponseList initialEventList) {
        this.initialEventList = initialEventList;
    }

    /**
     * Tells us whether this message represents successful program creation.
     * Server/port information in this message can only be treated as valid
     * if this returns true.
     * @return
     */
    public boolean isCreationSuccessful() {
        return this.creationSuccessful;
    }
}
