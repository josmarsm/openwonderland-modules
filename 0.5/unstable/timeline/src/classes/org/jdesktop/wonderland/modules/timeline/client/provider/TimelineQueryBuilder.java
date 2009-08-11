/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.client.provider;

import javax.swing.JComboBox;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;

/**
 * Interface for an object that lets you build a query.  TimelineQueryBuilder
 * classes should be annotated with the <code>@QueryBuilder</code> annotation.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public interface TimelineQueryBuilder {
    /**
     * The display name for this builder
     * @return the display name for the builder
     */
    public String getDisplayName();

    /**
     * Get the class of TimelineProvider this builder builds queries for.
     * This must match the value returned by the <code>getQueryClass()</code>
     * method of the resulting <code>TimelineQuery</code> object.  It is used
     * to match queries to the builder for that query.
     */
    public String getQueryClass();

    /**
     * Set the timeline cell configuration this builder is using.  This method
     * will be called every time the timeline configuration changes.
     * @param config the timeline configuration
     */
    public void setTimelineConfiguration(TimelineConfiguration config);

    /**
     * Set the current value of a query.  This will pass in a query object
     * whose queryClass field matches the result of calling
     * <code>getQueryClass()</code> on this builder.  It should update the
     * UI to match the state of the given query.
     * @param query the current query.
     */
    public void setQuery(TimelineQuery query);

    /**
     * Get the combo box for first-level configuration of this
     * object.  The combo box is used for high-level query configuration.
     * The result may be null for a provider that doesn't need to expose a
     * combo box.
     * @return the combo box or null for no combo box
     */
    public JComboBox getConfigurationComboBox();

    /**
     * Get the panel for advanced configuration of this provider.  The panel
     * may be null for a provider that doesn't expose advanced configuration.
     * @return the panel for advanced configuration.
     */
    public JComboBox getConfigurationPanel();

    /**
     * Get the configured query this builder provides.
     * @return the configured query
     */
    public TimelineQuery build();
}
