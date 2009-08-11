/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.timeline.common.provider;

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
public class TimelineQuery {
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
    }
    
    /**
     * Create a new TimelineQuery
     * @param queryClass the class name of the class on the provider which
     * executes this query
     */
    public TimelineQuery(String queryClass) {
        this.queryClass = queryClass;
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
        String key;
        String value;
    }
}
