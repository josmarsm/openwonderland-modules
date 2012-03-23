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
package uk.ac.essex.wonderland.modules.twitter.server;

import com.sun.sgs.app.AppContext;
import com.sun.sgs.app.ManagedReference;
import com.sun.sgs.app.PeriodicTaskHandle;
import com.sun.sgs.app.Task;
import com.sun.sgs.app.TaskManager;
import java.io.Serializable;
import org.jdesktop.wonderland.common.cell.ClientCapabilities;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import org.jdesktop.wonderland.common.cell.state.CellServerState;
import uk.ac.essex.wonderland.modules.twitter.common.TwitterSearchRequestMessage;
import uk.ac.essex.wonderland.modules.twitter.common.TwitterCellClientState;
import uk.ac.essex.wonderland.modules.twitter.common.TwitterCellServerState;
import org.jdesktop.wonderland.server.cell.AbstractComponentMessageReceiver;
import org.jdesktop.wonderland.server.cell.CellMO;
import org.jdesktop.wonderland.server.cell.ChannelComponentMO;
import org.jdesktop.wonderland.server.comms.WonderlandClientID;
import org.jdesktop.wonderland.server.comms.WonderlandClientSender;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import uk.ac.essex.wonderland.modules.twitter.common.TwitterQueryResultMessage;
import uk.ac.essex.wonderland.modules.twitter.server.TwitterManager.TwitterQueryListener;
import uk.ac.essex.wonderland.modules.twitter.server.TwitterManager.TwitterQueryResult;

/**
 * A server cell associated with a twitter app.
 *
 * @author Bernard Horan
 */
public class TwitterCellMO extends CellMO implements TwitterQueryListener {

    private QueryResult queryResult;
    private PeriodicTaskHandle queryTaskHandle = null;

    /** Default constructor, used when the cell is created via WFS */
    public TwitterCellMO() {
        super();
    }

    @Override
    protected String getClientCellClassName(WonderlandClientID clientID, ClientCapabilities capabilities) {
        return "uk.ac.essex.wonderland.modules.twitter.client.TwitterCell";
    }

    @Override
    protected CellClientState getClientState(CellClientState cellClientState, WonderlandClientID clientID, ClientCapabilities capabilities) {
        if (cellClientState == null) {
            cellClientState = new TwitterCellClientState(queryResult);
        }
        return super.getClientState(cellClientState, clientID, capabilities);
    }

    @Override
    public CellServerState getServerState(CellServerState stateToFill) {
        if (stateToFill == null) {
            stateToFill = new TwitterCellServerState();
        }
        super.getServerState(stateToFill);
        return stateToFill;
    }

    @Override
    protected void setLive(boolean live) {
        super.setLive(live);

        ChannelComponentMO channel = getComponent(ChannelComponentMO.class);
        if (live == true) {
            channel.addMessageReceiver(TwitterSearchRequestMessage.class,
                    (ChannelComponentMO.ComponentMessageReceiver) new SearchRequestMessageReceiver(this));
            createPeriodicTask();
        } else {
            channel.removeMessageReceiver(TwitterSearchRequestMessage.class);
            if (queryTaskHandle != null) {
                queryTaskHandle.cancel();
            }
        }
    }

    private void runQuery(String queryString) {
        //If this is a new query string, invalidate the old results
        if (queryResult != null) {
            if (!queryString.equalsIgnoreCase(queryResult.getQuery())) {
                queryResult = null;
            }
        }
        Twitter twitter = new TwitterFactory().getInstance();
        TwitterManager tm = AppContext.getManager(TwitterManager.class);
        //Call the method on the service
        //Callbacks to this object via twitterQueryResult()
        Query query = new Query(queryString);
        tm.runQuery(twitter, query, this);
    }

    public void twitterQueryResult(TwitterQueryResult result) {
        queryResult = result.getQueryResult();
        sendQueryResponse();
    }

    private void sendQueryResponse() {
        sendCellMessage(null, new TwitterQueryResultMessage(getCellID(), queryResult));
    }

    private void createPeriodicTask() {
        TaskManager tm = AppContext.getTaskManager();
        Task queryTask = new QueryTask(this);
        queryTaskHandle = tm.schedulePeriodicTask(queryTask, 0, 30000); //run the task now and then every 30 seconds

    }

    private static class SearchRequestMessageReceiver extends AbstractComponentMessageReceiver {
        public SearchRequestMessageReceiver(TwitterCellMO cellMO) {
            super(cellMO);
        }

        public void messageReceived(WonderlandClientSender sender, WonderlandClientID clientID, CellMessage message) {
            TwitterSearchRequestMessage tsrm = (TwitterSearchRequestMessage) message;
            TwitterCellMO cellMO = (TwitterCellMO) getCell();
            cellMO.runQuery(tsrm.getQuery());
        }        
    }

    private static class QueryTask implements Task, Serializable {

        private ManagedReference<TwitterCellMO> cellRef = null;

        public QueryTask(TwitterCellMO cellMO) {
            cellRef = AppContext.getDataManager().createReference(cellMO);
        }

        public void run() throws Exception {
            QueryResult queryResult = cellRef.get().queryResult;
            if (queryResult != null) {
                Twitter twitter = new TwitterFactory().getInstance();
                TwitterManager tm = AppContext.getManager(TwitterManager.class);
                //Call the method on the service
                //Callbacks to this object via twitterQueryResult()
                Query query = new Query(queryResult.getQuery());
                //Don't get any results we've already processed
                query.setSinceId(queryResult.getMaxId());
                tm.runQuery(twitter, query, cellRef.get());
            }
        }
    }
}
