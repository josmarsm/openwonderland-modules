/*
 *  +Spaces Project, http://www.positivespaces.eu/
 *
 *  Copyright (c) 2010-12, University of Essex, UK, 2010-12, All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package uk.ac.essex.wonderland.modules.postercontrol.web;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Record to represent a Poster
 * @author Bernard Horan
 */
@XmlRootElement
public class PosterRecord {

    @XmlElement
    private int cellID;
    @XmlElement
    private String cellName;
    private String posterContents;
    private List<PosterAction> actions;

    public PosterRecord(int cellID, String cellName, String posterContents) {
        this.cellID = cellID;
        this.cellName = cellName;
        this.posterContents = posterContents;
        this.actions = new ArrayList<PosterAction>();
    }

    public PosterRecord() {
        
    }

    public int getCellID() {
        return cellID;
    }

    public String getCellName() {
        return cellName;
    }

    public String getPosterContents() {
        return posterContents;
    }

    public void addAction(PosterAction action) {
        actions.add(action);
    }

    public List<PosterAction> getActions() {
        return actions;
    }

    /**
     * Test equality of poster records
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof PosterRecord) {
            return cellID == ((PosterRecord)o).cellID;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + this.cellID;
        return hash;
    }

    public void setPosterContents(String contents) {
        posterContents = contents;
    }
}
