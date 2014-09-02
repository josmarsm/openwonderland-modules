/**
 * Copyright (c) 2014, WonderBuilders, Inc., All Rights Reserved
 */

/**
 * Open Wonderland
 *
 * Copyright (c) 2011, Open Wonderland Foundation, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The Open Wonderland Foundation designates this particular file as
 * subject to the "Classpath" exception as provided by the Open Wonderland
 * Foundation in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.wonderland.client.cell.asset.AssetUtils;
import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
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
        // see if there is a currently running test
        TestSection cur = getCurrentSection();
        if (cur instanceof RunnableTestSection) {
            Test curTest = ((RunnableTestSection) cur).getCurrentTest();
            if (curTest != null) {
                curTest.appendMessage(message);
            }
        }
        
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
           
        ClassLoader testClassLoader = createTestClassLoader(config);
        Test test = create(className, Test.class, testClassLoader);
        test.initialize(config);
        
        // store our record of the test
        tests.put(test.getId(), test);
        
        return test;
    }
    
    private ClassLoader createTestClassLoader(JSONObject config) 
            throws IOException
    {
        List<URL> urls = new ArrayList<URL>(Arrays.asList(new URL[] {
           getAssetURL("wla://client-test/client-test-tests.jar") 
        }));
        
        if (config.containsKey("libraries")) {
            JSONArray libraries = (JSONArray) config.get("libraries");
            for (int i = 0; i < libraries.size(); i++) {
                urls.add(getAssetURL((String) libraries.get(i)));
            }
        }
        
        LOGGER.log(Level.INFO, "Creating test with classpath: {0}",
                   urls.toString());
        
        return new ScannedClassLoader(urls.toArray(new URL[0]),
                                      getClass().getClassLoader());
    }
    
    private <T> T create(String className, Class<T> clazz) 
            throws IOException
    {
        return create(className, clazz, getClass().getClassLoader());
    }
    
    private <T> T create(String className, Class<T> clazz,
                         ClassLoader loader) 
            throws IOException
    {
        try {
            Class<T> c = (Class<T>) loader.loadClass(className);
            return c.newInstance();
        } catch (InstantiationException ex) {
            throw new IOException(ex);
        } catch (IllegalAccessException ex) {
            throw new IOException(ex);
        } catch (ClassNotFoundException ex) {
            throw new IOException(ex);
        }
    }
    
    public static URL getAssetURL(String asset) throws IOException {
        return AssetUtils.getAssetURL(asset, getServerNameAndPort());
    }
    
    private static String getServerNameAndPort() throws MalformedURLException {
        URL serverURL = new URL(System.getProperty("jnlp.wonderland.server.url"));
        return serverURL.getHost() + ":" + serverURL.getPort();
    }
    
    public interface TestListener {
        void sectionChanged(TestSection current, boolean autoStart);
        void sectionComplete(TestSection current);
    }
}
