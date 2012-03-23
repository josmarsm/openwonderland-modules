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

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import twitter4j.Tweet;
import uk.ac.essex.wonderland.modules.twitter.client.TwitterPanel.TweetIconRenderer;

/**
 * TableModel that adapts to a list of tweets, holds a maximum number
 * of Tweets.
 * @author Bernard Horan
 */
class TweetTableModel extends AbstractTableModel{
    private static final String[] COLUMN_NAMES = {"Icon", "Text"};
    /** Maximum number of tweets to keep */
    private static final int MAX_TWEET_COUNT = 99;
    private List<Tweet> tweetList = new ArrayList<Tweet>();
    private Map<Tweet, ImageIcon> imageCache = new WeakHashMap<Tweet, ImageIcon>();

    public TweetTableModel() {
        
    }

    public int getRowCount() {
        return tweetList.size();
    }

    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }


    public Object getValueAt(int row, int column) {
        Tweet selectedTweet = tweetList.get(row);
        if (column == 0) {
            return getIcon(selectedTweet);
        } else {
            return new TweetCell(selectedTweet); //Workaround because can't return an instance of Tweet for renderer
        }
        
    }

    private Icon getIcon(Tweet aTweet) {
        //Look in the cache
        ImageIcon profileIcon = imageCache.get(aTweet);
        if (profileIcon != null) {
            return profileIcon;
        }
        //Not in the cache
        try {
            URL profileImageURL = new URL(aTweet.getProfileImageUrl());
            //Create an image icon from the URL
            profileIcon = new ImageIcon(profileImageURL, aTweet.getFromUser());
            int iconHeight = profileIcon.getIconHeight();
            //If the icon is wrong size
            if (iconHeight != 48) {
                Image img = profileIcon.getImage();
                Image scaledImage = img.getScaledInstance(TweetIconRenderer.ICON_DIM, TweetIconRenderer.ICON_DIM, Image.SCALE_SMOOTH);
                profileIcon = new ImageIcon(scaledImage);
            }
            //put the image in the cache
            imageCache.put(aTweet, profileIcon);
            return profileIcon;
        } catch (MalformedURLException ex) {
            Logger.getLogger(TwitterPanel.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    void addTweet(Tweet aTweet) {
        tweetList.add(0, aTweet);
        fireTableRowsInserted(0, 0);
        if (tweetList.size() > MAX_TWEET_COUNT) {
            tweetList.remove(MAX_TWEET_COUNT);
            fireTableRowsDeleted(MAX_TWEET_COUNT, MAX_TWEET_COUNT);
        }
    }

    public class TweetCell {
        private String fromUser;
        private String text;

        TweetCell(Tweet aTweet) {
            this.fromUser = aTweet.getFromUser();
            this.text = aTweet.getText();
        }

        String getFromUser() {
            return fromUser;
        }

        String getText() {
            return text;
        }
    }
}
