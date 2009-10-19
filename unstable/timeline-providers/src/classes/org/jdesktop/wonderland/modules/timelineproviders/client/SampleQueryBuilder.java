/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timelineproviders.client;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.jdesktop.wonderland.modules.timeline.client.provider.TimelineQueryBuilder;
import org.jdesktop.wonderland.modules.timeline.client.provider.annotation.QueryBuilder;
import org.jdesktop.wonderland.modules.timeline.common.TimelineConfiguration;
import org.jdesktop.wonderland.modules.timeline.common.provider.TimelineQuery;

/**
 * Testing query builder
 * @author Joanthan Kaplan <kaplanj@dev.java.net>
 */
@QueryBuilder
public class SampleQueryBuilder implements TimelineQueryBuilder {

    public String getDisplayName() {
        return "Sample";
    }

    public String getQueryClass() {
        return "org.jdesktop.wonderland.modules.timelineproviders.provider.SampleProvider";
    }

    public void setTimelineConfiguration(TimelineConfiguration config) {
    }

    public void setQuery(TimelineQuery query) {
    }

    public JComboBox getConfigurationComboBox() {
        return null;
    }

    public JPanel getConfigurationPanel() {
        return null;
    }

    public TimelineQuery build() {
        TimelineQuery query = new TimelineQuery(getQueryClass());
        query.getProperties().setProperty("test", "123");

        return query;
    }
}
