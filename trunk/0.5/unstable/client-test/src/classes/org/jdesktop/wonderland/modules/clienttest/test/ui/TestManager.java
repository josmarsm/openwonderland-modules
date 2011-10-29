/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author jkaplan
 */
public enum TestManager {
    INSTANCE;
    
    private static final Logger LOGGER =
            Logger.getLogger(TestManager.class.getName());
    
    // listeners
    private final List<TestListener> listeners =
            new CopyOnWriteArrayList<TestListener>();
    
    // all sections
    private final List<TestSection> sections = new ArrayList<TestSection>();
    
    // index of the current test section
    private int currentIndex = 0;

    // the current test section
    private TestSection currentSection;
    
    // the next id to assign
    private int nextId = 0;
    
    // a map from test id to the test itself
    private Map<String, Test> tests = new LinkedHashMap<String, Test>();
    
    // log messages
    private final StringBuilder logBuffer = new StringBuilder();
    
    public void initialize() {
        sections.addAll(loadTestConfig());
        currentSection = sections.get(currentIndex);
    }
    
    public List<TestSection> getSections() {
        return sections;
    }
    
    public TestSection getCurrentSection() {
       return currentSection;
    }
    
    public void setCurrentSection(TestSection section) {
        currentIndex = getSections().indexOf(section);
        currentSection = getSections().get(currentIndex);
        
        // set section and update listeners
        fireSectionChanged(currentSection, false);
    }
    
    public boolean hasNextSection() {
        return currentIndex < sections.size() - 1;
    }
    
    public boolean hasPreviousSection() {
        return currentIndex > 0;
    }
    
    public TestSection nextSection() {
        currentIndex++;
        currentSection = getSections().get(currentIndex);
        fireSectionChanged(currentSection, true);
        return currentSection;
    }
    
    public TestSection previousSection() {
        currentIndex--;
        currentSection = getSections().get(currentIndex);
        fireSectionChanged(currentSection, false);
        return currentSection;
    }
    
    public void sectionComplete() {
        fireSectionComplete(currentSection);
    }
    
    public Collection<Test> getTests() {
        return tests.values();
    }
    
    public Test getTest(String testId) {
        return tests.get(testId);
    }
    
    public void addTestListener(TestListener listener) {
        listeners.add(listener);
    }
    
    public void removeTestListener(TestListener listener) {
        listeners.remove(listener);
    }
    
    public String nextTestId() {
        return "Test" + nextId++;
    }
    
    public void appendToLog(String message) {
        logBuffer.append(message);
    }
    
    public String getLog() {
        return logBuffer.toString();
    }
    
    protected void fireSectionChanged(TestSection current, boolean autoStart) {
        for (TestListener listener : listeners) {
            listener.sectionChanged(current, autoStart);
        }
    }
    
    protected void fireSectionComplete(TestSection current) {
        for (TestListener listener : listeners) {
            listener.sectionComplete(current);
        }
    }
     
    private List<TestSection> loadTestConfig() {
        List<TestSection> out = new ArrayList<TestSection>();
        
        try {
            URL configURL = TestManager.class.getResource("resources/TestConfig.json");
            JSONArray js = (JSONArray) 
                    JSONValue.parse(new InputStreamReader(configURL.openStream()));
            
            for (Object section : js) {
                if (section instanceof String) {
                    out.add(createSection((String) section, null));
                } else if (section instanceof JSONArray) {
                    JSONArray ja = (JSONArray) section;
                    String name = (String) ja.get(0);
                    JSONObject config = (JSONObject) ja.get(1);
                    
                    out.add(createSection(name, config));
                } else {
                    throw new IOException("Unexpected type: " + section);
                }
            }
            
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        
        return out;
    }
    
    private TestSection createSection(String className, JSONObject config)
            throws IOException
    {
        if (config == null) {
            config = new JSONObject();
        }
        
        TestSection section = create(className, TestSection.class);
        section.initialize(config);
        return section;
    }
    
    List<Test> loadTests(JSONArray tests) {
        List<Test> out = new ArrayList<Test>();
        
        try {
            for (Object test : tests) {
                if (test instanceof String) {
                    out.add(createTest((String) test, null));
                } else if (test instanceof JSONArray) {
                    JSONArray ja = (JSONArray) test;
                    String name = (String) ja.get(0);
                    JSONObject config = (JSONObject) ja.get(1);
                    
                    out.add(createTest(name, config));
                } else {
                    throw new IOException("Unexpected type " + test);
                }
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        
        return out;
    }
    
    private Test createTest(String className, JSONObject config) 
            throws IOException
    {
        if (config == null) {
            config = new JSONObject();
        }
           
        Test test = create(className, Test.class);
        test.initialize(config);
        
        // store our record of the test
        tests.put(test.getId(), test);
        
        return test;
    }
    
    private <T> T create(String className, Class<T> clazz) 
            throws IOException
    {
        try {
            Class<T> c = (Class<T>) Class.forName(className);
            return c.newInstance();
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        }
    }
    
    public interface TestListener {
        void sectionChanged(TestSection current, boolean autoStart);
        void sectionComplete(TestSection current);
    }
}
