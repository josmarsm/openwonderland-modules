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
package uk.ac.essex.wonderland.modules.twitter.common;

import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import twitter4j.QueryResult;

/**
 * Message sent <em>from server to client</em> to return a query result.
 * @author Bernard Horan
 */
public class TwitterQueryResultMessage extends CellMessage {
    private final QueryResult queryResult;
    
    public TwitterQueryResultMessage(CellID cellID, QueryResult queryResult) {
        super(cellID);
        this.queryResult = queryResult;
    }

    public QueryResult getQueryResult() {
        return queryResult;
    }

    
}
