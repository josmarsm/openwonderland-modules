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
package org.jdesktop.wonderland.modules.timeline.client.provider;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.jdesktop.wonderland.modules.timeline.client.FlickrConfigurationPanel;
import org.jdesktop.wonderland.modules.timeline.client.provider.annotation.QueryBuilder;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;

/**
 * A query builder for Flickr queries
 * @author nsimpson
 */
@QueryBuilder
public class FlickrTimelineQueryBuilder implements TimelineQueryBuilder {
    private static final String DISPLAY_NAME = "Flickr";
    private static final String QUERY_CLASS = "org.jdesktop.wonderland.modules.timeline.server.provider.FlickrTimelineProvider";
    private TimelineConfiguration config;
    private TimelineQuery query;
    private JComboBox configComboBox;
    private JPanel configPanel;

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
        ComboBoxModel model = comboBox.getModel();
        return comboBox;
    }

    /**
     * Create the query configuration panel
     * @return a query configuration panel
     */
    private JPanel createConfigPanel() {
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

    /**
     * Get the configured query this builder provides.
     * @return the configured query
     */
    public TimelineQuery build() {
        query = new TimelineQuery(QUERY_CLASS);
        // TODO: configure the query with the query parameters from the UI
        return new TimelineQuery(QUERY_CLASS);
    }
}
