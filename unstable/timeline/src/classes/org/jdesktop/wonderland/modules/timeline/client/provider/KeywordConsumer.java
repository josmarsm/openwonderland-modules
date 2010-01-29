/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.client.provider;

import java.util.List;

/**
 * A query builder that additionally accepts keywords per increment.
 * The keywords will be set after the timeline configuration.
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
public interface KeywordConsumer {
    /**
     * Set the keywords for this query. The argument is a list with
     * one entry for each increment in the timeline configuration.  This
     * method will be called immediately before build().
     * @param keywords the list of keywords by increment
     */
    public void setKeywords(List<String> keywords);
}
