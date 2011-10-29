/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * IntroTestSection.java
 *
 * Created on Oct 18, 2011, 11:34:09 AM
 */
package org.jdesktop.wonderland.modules.clienttest.test.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author jkaplan
 */
public class TestsSection extends javax.swing.JPanel
    implements RunnableTestSection 
{
    private static final Logger LOGGER =
            Logger.getLogger(TestsSection.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/clienttest/test/ui/resources/Bundle");
 
    private String title;
    private final List<Test> tests = new ArrayList<Test>();
    
    private SwingWorker worker;
    private TestResult result = TestResult.NOT_RUN;
    
    /** Creates new form IntroTestSection */
    public TestsSection() {
    }

    public void initialize(JSONObject config) {
        initComponents();
        textPane.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    handleLink(e.getDescription());
                }
            }
        });
        
        
        title = BUNDLE.getString((String) config.get("title"));
        
        JSONArray jTests = (JSONArray) config.get("tests");
        tests.addAll(TestManager.INSTANCE.loadTests(jTests));
        
        update();
    }

    public String getName() {
        return title;
    }

    public JPanel getPanel() {
        return this;
    }
    
    public void sectionVisible() {
        update();
    }
    
    public void sectionStarted() {
        // clear any previous results and message
        for (Test test : tests) {
            test.setResult(TestResult.NOT_RUN);
            test.clearMessages();
        }
        setResult(TestResult.NOT_RUN);
        
        startTest(tests.get(0));
    }

    public void sectionSkipped() {
        if (worker != null) {
            worker.cancel(true);
            worker = null;
        }
    }    
    
    public TestResult getResult() {
        return result;
    }

    protected void setResult(TestResult result) {
        this.result = result;
    }
    
    protected void testComplete(Test test, TestResult result) {
        Iterator<Test> ti = tests.iterator();
        while (ti.hasNext()) {
            if (test.equals(ti.next())) {
                test.setResult(result);
                
                if (ti.hasNext()) {
                    startTest(ti.next());
                } else {
                    testsComplete();
                }
                
                return;
            }
        }
        
        // not found!
        testsComplete();
    }
    
    protected void startTest(Test test) {
        test.setResult(TestResult.IN_PROGRESS);
        LOGGER.log(Level.INFO, MessageFormat.format(
                BUNDLE.getString("Starting_Test"), test.getName(), test.getId()));
                
        worker = new TestWorker(test);
        worker.execute();
        
        update();
    }
    
    protected void testsComplete() {
        // set the result to the worst of all test run
        TestResult worst = TestResult.NOT_RUN;
        for (Test test : tests) {
            if (test.getResult().ordinal() > worst.ordinal()) {
                worst = test.getResult();
            }
        }
        setResult(worst);
        
        TestManager.INSTANCE.sectionComplete();
        update();
    }
    
    protected void update() {
        textPane.setText(getText());
    }
    
    protected String getText() {
        StringBuilder out = new StringBuilder();
        out.append("<h1>").append(title).append("</h1>");
        
        out.append("<table cellpadding=\"5px\">");
        for (Test test : tests) {
            out.append("<tr>").append("<td>");
            out.append(resultImage(test.getResult()));
            out.append("</td><td><font size=\"6\">");
            
            if (test.getMessages().length() != 0) {
                out.append("<a href=").append(test.getId()).append(">");
            }
            
            out.append(test.getName());
            
            if (test.getMessages().length() != 0) {
                out.append("</a>");
            }
            
            out.append("</font></td>").append("</tr>");
        }
        out.append("</table>");
        
        return out.toString();
    }
    
    
    protected void handleLink(String testId) {
        Test test = TestManager.INSTANCE.getTest(testId);
        JOptionPane.showMessageDialog(this, test.getMessages());
    }
    
    public static String resultImage(TestResult result) {
        StringBuilder out = new StringBuilder();
        out.append("<img src=\"");
        
        URL url = TestsSection.class.getResource("resources/" + result.getIcon());
        out.append(url.toExternalForm());
        
        out.append("\">");
        return out.toString();
    }

    private class TestWorker extends SwingWorker<TestResult, Object> {
        private final Test test;
        
        public TestWorker(Test test) {
            this.test = test;
        }
        
        @Override
        protected TestResult doInBackground() throws Exception {
            return test.run();
        }

        @Override
        protected void done() {
            try {
                testComplete(test, get());
            } catch (InterruptedException ie) {
                testComplete(test, TestResult.NOT_RUN);
            } catch (CancellationException ce) {
                testComplete(test, TestResult.NOT_RUN);
            } catch (ExecutionException ee) {
                LOGGER.log(Level.WARNING, "Error executing test", ee);
                testComplete(test, TestResult.FAIL);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        textPane = new javax.swing.JEditorPane();

        setBackground(new java.awt.Color(255, 255, 255));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/jdesktop/wonderland/modules/clienttest/test/ui/resources/Bundle"); // NOI18N
        textPane.setContentType(bundle.getString("TestsSection.textPane.contentType")); // NOI18N
        textPane.setEditable(false);
        jScrollPane1.setViewportView(textPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JEditorPane textPane;
    // End of variables declaration//GEN-END:variables

}
