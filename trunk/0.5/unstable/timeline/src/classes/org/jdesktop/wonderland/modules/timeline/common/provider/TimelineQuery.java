/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.common.provider;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Properties;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * An interface for configurable queries of the timeline.  This class may be
 * subclassed to add additional properties to the query.  Instances of
 * TimelineQuery must be both Java serializable and also JAXB serializable.
 * They must also be annotated with the <code>@Query</code> annotation.
 *
 * @author Jonathan Kaplan <kaplanj@dev.java.net>
 */
@XmlRootElement(name="timeline-query")
public class TimelineQuery implements Serializable {
    /** The unique ID of this query. */
    private TimelineQueryID queryID;

    /**
     * the fully-qualified class name of the provider object that should be
     * used to exectute this query.
     */
    private String queryClass;
    
    /** properties for configuring the query */
    private Properties props = new Properties();
    
    /**
     * Default constructor
     */
    public TimelineQuery() {
        this (null);
    }
    
    /**
     * Create a new TimelineQuery
     * @param queryClass the class name of the class on the provider which
     * executes this query
     */
    public TimelineQuery(String queryClass) {
        this (null, queryClass);
    }

    /**
     * Create a new TimelineQuery
     * @param queryID the unique id of this query
     * @param queryClass the class of this query
     */
    public TimelineQuery(TimelineQueryID queryID, String queryClass) {
        this.queryID = queryID;
        this.queryClass = queryClass;
    }

    /**
     * Get the ID of this query. The ID is assigned by the server when
     * the query is added to the server. Queries with a null id will be
     * assigned a unique id when the query is added.
     * @return the id
     */
    @XmlElement
    @XmlJavaTypeAdapter(QueryIDAdapter.class)
    public TimelineQueryID getQueryID() {
        return queryID;
    }

    /**
     * Set the ID of this query
     * @param queryID the queryID to set
     */
    public void setQueryID(TimelineQueryID queryID) {
        this.queryID = queryID;
    }
    
    /**
     * Get the query class for this query.  This is the fully qualifies name
     * of the provider object that will be used to execute this query.
     * @return the query class
     */
    @XmlElement
    public String getQueryClass() {
        return queryClass;
    }
    
    /**
     * Set the query class
     */
    public void setQueryClass(String queryClass) {
        this.queryClass = queryClass;
    }
    
    /**
     * Get the properties for this query
     * @return the properties for this query
     */
    @XmlElement
    @XmlJavaTypeAdapter(PropertiesAdapter.class)
    public Properties getProperties() {
        return props;
    }
    
    /**
     * Set the properties for this query
     * @param properties the properties
     */
    public void setProperties(Properties props) {
        this.props = props;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimelineQuery other = (TimelineQuery) obj;
        if (this.queryID != other.queryID &&
                (this.queryID == null || !this.queryID.equals(other.queryID)))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.queryID != null ? this.queryID.hashCode() : 0);
        return hash;
    }

    private static final class QueryIDAdapter
            extends XmlAdapter<Integer, TimelineQueryID>
    {

        @Override
        public TimelineQueryID unmarshal(Integer v) throws Exception {
            return new TimelineQueryID(v);
        }

        @Override
        public Integer marshal(TimelineQueryID v) throws Exception {
            return v.getID();
        }
    }

    private static final class PropertiesAdapter 
            extends XmlAdapter<Property[], Properties> 
    {
        @Override
        public Properties unmarshal(Property[] v) {
            Properties out = new Properties();
            for (Property p : v) {
                out.setProperty(p.key, p.value);
            }
            
            return out;
        }

        @Override
        public Property[] marshal(Properties v) {
            Property[] out = new Property[v.size()];
            
            int i = 0;
            for (Entry<Object, Object> e : v.entrySet()) {
                Property p = new Property();
                p.key = (String) e.getKey();
                p.value = (String) e.getValue();
                
                out[i++] = p;
            }
            
            return out;
        }
    }

    private static final class Property {
        @XmlElement String key;
        @XmlElement String value;
    }
}
