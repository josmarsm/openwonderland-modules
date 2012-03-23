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
package uk.ac.essex.wonderland.modules.twitter.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.Cell;
import org.jdesktop.wonderland.client.cell.CellCache;
import org.jdesktop.wonderland.client.cell.CellRenderer;
import org.jdesktop.wonderland.client.cell.ChannelComponent;
import org.jdesktop.wonderland.client.cell.ChannelComponent.ComponentMessageReceiver;
import org.jdesktop.wonderland.client.cell.annotation.UsesCellComponent;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuActionListener;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.ContextMenuItemEvent;
import org.jdesktop.wonderland.client.contextmenu.SimpleContextMenuItem;
import org.jdesktop.wonderland.client.contextmenu.cell.ContextMenuComponent;
import org.jdesktop.wonderland.client.contextmenu.spi.ContextMenuFactorySPI;
import org.jdesktop.wonderland.client.jme.artimport.DeployedModel;
import org.jdesktop.wonderland.client.jme.artimport.LoaderManager;
import org.jdesktop.wonderland.client.jme.cellrenderer.ModelRenderer;
import org.jdesktop.wonderland.client.scenemanager.event.ContextEvent;
import org.jdesktop.wonderland.common.cell.CellID;
import org.jdesktop.wonderland.common.cell.state.CellClientState;
import uk.ac.essex.wonderland.modules.twitter.common.TwitterCellClientState;
import org.jdesktop.wonderland.common.cell.CellStatus;
import org.jdesktop.wonderland.common.cell.messages.CellMessage;
import org.jdesktop.wonderland.common.cell.state.ModelCellComponentServerState.TransparencyMode;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import uk.ac.essex.wonderland.modules.twitter.common.TwitterQueryResultMessage;
import uk.ac.essex.wonderland.modules.twitter.common.TwitterSearchRequestMessage;

/**
 * Client cell for the twitter app.
 *
 * @author Bernard Horan
 */
public class TwitterCell extends Cell {

    /** The cell client state message received from the server cell */
    private TwitterCellClientState clientState;
    private static final ResourceBundle bundle = ResourceBundle.getBundle("uk/ac/essex/wonderland/modules/twitter/client/resources/Bundle");
    private TwitterPanelUI ui;
    /** The queue to hold the twitter statuses */
    private BlockingQueue<Tweet> tweetQueue;
    /** The thread to take the statuses out of the queue and display them */
    private Thread updateTwitterThread;
    /*8 boolean to indicate suspended state of thread */
    private boolean twitterThreadSuspended = true;
    /** Context menu */
    private ContextMenuFactorySPI menuFactory = null;
    @UsesCellComponent private ContextMenuComponent contextComp = null;

    /**
     * Create an instance of TwitterCell.
     *
     * @param cellID The ID of the cell.
     * @param cellCache the cell cache which instantiated, and owns, this cell.
     */
    public TwitterCell(CellID cellID, CellCache cellCache) {
        super(cellID, cellCache);
        tweetQueue = new LinkedBlockingQueue<Tweet>();
        createUpdateTwitterThread();
    }

    /**
     * Initialize the cell with parameters from the server.
     *
     * @param state the client state with which initialize the cell.
     */
    @Override
    public void setClientState(CellClientState state) {
        super.setClientState(state);
        clientState = (TwitterCellClientState) state;
    }

     @Override
    protected CellRenderer createCellRenderer(RendererType rendererType) {
        if (rendererType == RendererType.RENDERER_JME) {
            try {
                DeployedModel m =
                       LoaderManager.getLoaderManager().getLoaderFromDeployment(AssetUtils.getAssetURL("wla://twitter/pigeon.dae/pigeon.dae.gz.dep"));

                ModelRenderer renderer = new ModelRenderer(this, m);
                //Rather than fix the model, fix the transparency
                renderer.setTransparency(TransparencyMode.INVERSE);
                return renderer;
            } catch (MalformedURLException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        return super.createCellRenderer(rendererType);
    }

    /**
     * This is called when the status of the cell changes.
     */
    @Override
    protected void setStatus(CellStatus status, boolean increasing) {
        super.setStatus(status, increasing);

        switch (status) {

            // The cell is now visible
            case ACTIVE:
                if (increasing) {
                    

                    //Add menu item to open a tape to the right-hand button context menu
                    if (menuFactory == null) {
                        final ContextMenuActionListener l = new ContextMenuActionListener() {

                            public void actionPerformed(ContextMenuItemEvent event) {
                                openControlPanel();
                            }
                        };
                        menuFactory = new ContextMenuFactorySPI() {

                            public ContextMenuItem[] getContextMenuItems(ContextEvent event) {
                                return new ContextMenuItem[]{
                                            new SimpleContextMenuItem(bundle.getString("OPEN CONTROL PANEL"), l)
                                        };
                            }
                        };
                        contextComp.addContextMenuFactory(menuFactory);
                    }

                    twitterThreadSuspended = false;
                    notify();
                }
                break;
            case RENDERING:
                if (ui == null) {
                    initUI();
                }
                if (increasing) {
                    TwitterTimelineMessageReceiver recv = new TwitterTimelineMessageReceiver();
                    getComponent(ChannelComponent.class).addMessageReceiver(TwitterQueryResultMessage.class, recv);
                }
                break;

            // The cell is no longer visible
            case DISK:
                if (!increasing) {
                    twitterThreadSuspended = true;
                }
                hideUI();
                ui = null;
                break;
        }
    }

    private void initUI () {
        ui = new TwitterPanelUI(this);
    }

    public void hideUI() {
        ui.setVisible(false);
    }

    public void showUI() {
        ui.setVisible(true);
    }

    private void openControlPanel() {
        showUI();
    }

    void performQuery(String query) {
        sendCellMessage(new TwitterSearchRequestMessage(getCellID(), query));
    }

    private void updateTweets() {
        try {
            Tweet tweet = tweetQueue.take();
            ui.addTweet(tweet);
            TweetEntity entity = new TweetEntity(this, tweet);
            entity.setVisible(true);
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, "Failed to take a tweet", ex);
        }
    }

    private void updateQueryResult(QueryResult queryResult) {
        ui.setQueryString(queryResult.getQuery());
        @SuppressWarnings("unchecked")
        List<Tweet> tweetList = queryResult.getTweets();
        for (Tweet tweet : tweetList) {
            try {
                tweetQueue.put(tweet);
            } catch (InterruptedException ex) {
                Logger.getLogger(TwitterCell.class.getName()).log(Level.SEVERE, "Failed to put tweet", ex);
            }
        }
     }

    private void createUpdateTwitterThread() {
        updateTwitterThread = new Thread() {

            @Override
            public void run() {
                while (true) {
                    updateTweets();
                    try {
                        Thread.sleep(3000); //sleep for 3 seconds

                        synchronized (this) {
                            while (twitterThreadSuspended) {
                                wait();
                            }
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        updateTwitterThread.start();
    }

    String getQueryString() {
        QueryResult result = clientState.getQueryResult();
        if (result != null) {
            return result.getQuery();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    List<Tweet> getTweetList() {
        QueryResult result = clientState.getQueryResult();
        if (result != null) {
            return result.getTweets();
        } else {
            return null;
        }
    }

    class TwitterTimelineMessageReceiver implements ComponentMessageReceiver {
        public void messageReceived(CellMessage message) {
            TwitterQueryResultMessage tqrm = (TwitterQueryResultMessage)message;
            QueryResult result = tqrm.getQueryResult();
            updateQueryResult(result);
        }
    }
}
