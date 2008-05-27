/**
 * Project Looking Glass
 *
 * $RCSfile$
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * $Revision$
 * $Date$
 * $State$
 */
package org.jdesktop.wonderland.worldbuilder.persistence;

import org.jdesktop.wonderland.worldbuilder.Cell;

/**
 * When updating a cell tree, any cells that have not been modified
 * are replaced with instances of unmodified cell.  Only the 
 * <code>getCellID()</code> and <code>getParent()</code> methods of an 
 * unmodified cell are valid.  The result of calling other methods are 
 * undefined.
 * @author jkaplan
 */
public class UnmodifiedCell extends Cell {
    public UnmodifiedCell(String cellID) {
        super (cellID);
    }
}
