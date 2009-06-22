/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
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
package org.jdesktop.wonderland.modules.metadata.common.messages;

import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.modules.metadata.common.Metadata;

/**
 * Metadata Message.
 * @author mabonner
 */
public class MetadataMessage extends CellMessage {
    public enum Action {ADD, REMOVE, MODIFY};
    public Action action;
    public Metadata metadata;


    public MetadataMessage(){
        // a dummy for the test function
    }

    public MetadataMessage(Action act, Metadata meta){
        action = act;
        metadata = meta;
    }


}
