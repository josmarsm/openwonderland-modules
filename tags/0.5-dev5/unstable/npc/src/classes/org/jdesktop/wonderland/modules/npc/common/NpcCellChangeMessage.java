/* This code was developed with funding from the project "España Virtual"
 * 
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *  
 * "España Virtual es un proyecto de I+D, subvencionado por el CDTI dentro del
 * programa Ingenio 2010, orientado a la definición de la arquitectura,
 * protocolos y estándares del futuro Internet 3D, con un foco especial en lo
 * relativo a visualización 3D, inmersión en mundos virtuales, interacción
 * entre usuarios y a la introducción de aspectos semánticos, sin dejar de lado
 * el estudio y maduración de las tecnologías para el procesamiento masivo y
 * almacenamiento de datos geográficos.
 *
 * Con una duración de cuatro años, el proyecto está liderado por DEIMOS Space
 * y cuenta con la participación del Centro Nacional de Información Geográfica
 * (IGN/CNIG), Grid Systems, Indra Espacio, GeoVirtual, Androme Ibérica,
 * GeoSpatiumLab, DNX y una decena de prestigiosos centros de investigación y
 * universidades nacionales."
 */

package org.jdesktop.wonderland.modules.npc.common;

import com.jme.math.Vector3f;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;


/**
 *
 * @author david <dmaroto@it.uc3m.es> 
 */
public class NpcCellChangeMessage extends CellMessage{
    
    private Vector3f npcPosition;
    
    public Vector3f getNpcPosition(){
        return npcPosition;
    }
    
    public void setNpcPosition(Vector3f npcPosition){
        this.npcPosition = npcPosition;
    }
    
    public NpcCellChangeMessage(CellID cellID, Vector3f npcPosition) {
        super(cellID);
        this.npcPosition = npcPosition;
    }

    
}