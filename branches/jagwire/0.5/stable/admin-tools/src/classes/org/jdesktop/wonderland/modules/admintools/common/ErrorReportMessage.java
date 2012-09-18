/**
 * Open Wonderland
 *
 * Copyright (c) 2012, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.admintools.common;

import java.math.BigInteger;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;

/**
 * Message requesting a user submit an error report
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class ErrorReportMessage extends CellMessage {
    private final BigInteger sessionID;
    private final String reportID;
    
    public ErrorReportMessage(CellID cellID, BigInteger sessionID,
                              String reportID) 
    {
        super (cellID);
        this.sessionID = sessionID;
        this.reportID = reportID;
    }

    public BigInteger getSessionID() {
        return sessionID;
    }
    
    public String getReportID() {
        return reportID;
    }
}
