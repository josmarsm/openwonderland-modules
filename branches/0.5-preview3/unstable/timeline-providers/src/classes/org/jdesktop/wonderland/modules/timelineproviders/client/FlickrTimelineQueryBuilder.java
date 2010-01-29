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
package org.jdesktop.wonderland.modules.timelineproviders.client;

import java.awt.Dimension;
import java.util.List;
import java.util.Properties;
import org.jdesktop.wonderland.modules.timeline.client.provider.*;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.jdesktop.wonderland.modules.timeline.client.provider.annotation.QueryBuilder;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;
import org.jdesktop.wonderland.modules.timelineproviders.client.FlickrConfigurationPanel.SEARCH_TYPE;
import org.jdesktop.wonderland.modules.timelineproviders.client.FlickrConfigurationPanel.SORT_TYPE;
import org.jdesktop.wonderland.modules.timelineproviders.common.FlickrConstants;

/**
 * A query builder for Flickr queries
 * @author nsimpson
 */
@QueryBuilder
public class FlickrTimelineQueryBuilder 
        implements TimelineQueryBuilder, KeywordConsumer
{
    private static final String DISPLAY_NAME = "Flickr";
    private static final String QUERY_CLASS = "org.jdesktop.wonderland.modules.timelineproviders.provider.FlickrProvider";
    private TimelineConfiguration config;
    private TimelineQuery query;
    private JComboBox configComboBox;
    private FlickrConfigurationPanel configPanel;
    private List<String> keywords;

    public FlickrTimelineQueryBuilder() {
        configComboBox = createConfigComboBox();
        configPanel = createConfigPanel();
    }

    /**
     * Create a configuration combo box.
     * @return a configuration combo box
     */
    private JComboBox createConfigComboBox() {
        // TODO: configure the combo box
        JComboBox comboBox = new JComboBox();
        comboBox.setEditable(true);
        comboBox.setPreferredSize(new Dimension(83, 28));
        return comboBox;
    }

    /**
     * Create the query configuration panel
     * @return a query configuration panel
     */
    private FlickrConfigurationPanel createConfigPanel() {
        return new FlickrConfigurationPanel();
    }
    /**
     * The display name for this builder
     * @return the display name for the builder
     */
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    /**
     * Get the class of TimelineProvider this builder builds queries for.
     * This must match the value returned by the <code>getQueryClass()</code>
     * method of the resulting <code>TimelineQuery</code> object.  It is used
     * to match queries to the builder for that query.
     */
    public String getQueryClass() {
        return QUERY_CLASS;
    }

    /**
     * Set the timeline cell configuration this builder is using.  This method
     * will be called every time the timeline configuration changes.
     * @param config the timeline configuration
     */
    public void setTimelineConfiguration(TimelineConfiguration config) {
        this.config = config;
    }

    /**
     * Set the current value of a query.  This will pass in a query object
     * whose queryClass field matches the result of calling
     * <code>getQueryClass()</code> on this builder.  It should update the
     * UI to match the state of the given query.
     * @param query the current query.
     */
    public void setQuery(TimelineQuery query) {
        this.query = query;

        // set the configuration based on this query
        Properties props = query.getProperties();
        configComboBox.setSelectedItem(props.getProperty(FlickrConstants.SEARCH_TEXT_PROP));

        // set up the configuration panel
        if (props.containsKey(FlickrConstants.API_KEY_PROP)) {
            configPanel.setAPIKey(props.getProperty(FlickrConstants.API_KEY_PROP));
        }

        if (props.containsKey(FlickrConstants.SEARCH_TYPE_PROP)) {
            String t = props.getProperty(FlickrConstants.SEARCH_TYPE_PROP);
            SEARCH_TYPE type = (t.equals(FlickrConstants.SEARCH_TAGS_KEY)) ?
                SEARCH_TYPE.TAGS : SEARCH_TYPE.TEXT;
            configPanel.setSearchType(type);
        }

        if (props.containsKey(FlickrConstants.SORT_PROP)) {
            String s = props.getProperty(FlickrConstants.SORT_PROP);
            SORT_TYPE type = (s.equals(FlickrConstants.SORT_RELEVANCE_KEY)) ?
                SORT_TYPE.RELEVANCE : SORT_TYPE.INTEREST;
            configPanel.setSortType(type);
        }

        if (props.containsKey(FlickrConstants.RETURN_COUNT_PROP)) {
            configPanel.setResultLimit(Integer.parseInt(
                    props.getProperty(FlickrConstants.RETURN_COUNT_PROP)));
        }

        if (props.containsKey(FlickrConstants.CC_PROP)) {
            configPanel.setCreativeCommons(Boolean.parseBoolean(
                    props.getProperty(FlickrConstants.CC_PROP)));
        }
    }

    /**
     * Get the combo box for first-level configuration of this
     * object.  The combo box is used for high-level query configuration.
     * The result may be null for a provider that doesn't need to expose a
     * combo box.
     * @return the combo box or null for no combo box
     */
    public JComboBox getConfigurationComboBox() {
        return configComboBox;
    }

    /**
     * Get the panel for advanced configuration of this provider.  The panel
     * may be null for a provider that doesn't expose advanced configuration.
     * @return the panel for advanced configuration.
     */
    public JPanel getConfigurationPanel() {
        return configPanel;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    /**
     * Get the configured query this builder provides.
     * @return the configured query
     */
    public TimelineQuery build() {
        if (query == null) {
            query = new TimelineQuery(QUERY_CLASS);
        }

        // set default values
        setQueryDefaults(query);

        // get the query properties
        Properties props = query.getProperties();
        props.setProperty(FlickrConstants.SEARCH_TEXT_PROP,
                          (String) configComboBox.getSelectedItem());

        if ((!configPanel.isOK() || configPanel.isUseKeywords()) &&
                keywords != null)
        {
            for (int i = 0; i < keywords.size(); i++) {
                String keyword = keywords.get(i);
                if (keyword != null && keyword.trim().length() > 0) {
                    props.setProperty(FlickrConstants.KEYWORD_PROP + i, keyword);
                }
            }
        }

        // set the advanced configuration
        if (configPanel.isOK()) {
            props.setProperty(FlickrConstants.SEARCH_TYPE_PROP, 
                    configPanel.getSearchType().getKey());
            
            props.setProperty(FlickrConstants.SORT_PROP,
                    configPanel.getSortType().getKey());

            props.setProperty(FlickrConstants.RETURN_COUNT_PROP,
                    String.valueOf(configPanel.getResultLimit()));

            props.setProperty(FlickrConstants.CC_PROP,
                    String.valueOf(configPanel.isCreativeCommons()));
        }

        return query;
    }

    /**
     * Creat the default query
     * @return a default query
     */
    private void setQueryDefaults(TimelineQuery query) {
        Properties props = query.getProperties();

        // get the api key
        String apiKey = System.getProperty("flickr.api.key");
        if (apiKey == null) {
            apiKey = configPanel.getAPIKey();
        }
        props.setProperty(FlickrConstants.API_KEY_PROP, apiKey);

        // set the various dates
        props.setProperty(FlickrConstants.START_DATE_PROP,
                          String.valueOf(config.getDateRange().getMinimum().getTime()));
        props.setProperty(FlickrConstants.UNITS_PROP,
                          String.valueOf(config.getUnits().getCalendarUnit()));
        props.setProperty(FlickrConstants.INCREMENTS_PROP,
                          String.valueOf(config.getNumSegments()));
    }
}
