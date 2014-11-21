package org.jdesktop.wonderland.modules.isocial.tokensheet.client;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.net.URL;
import org.jdesktop.wonderland.modules.isocial.tokensheet.common.ResultType;
import org.jdesktop.wonderland.modules.isocial.tokensheet.common.Student;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.bind.JAXBException;
import org.jdesktop.wonderland.client.hud.CompassLayout.Layout;
import org.jdesktop.wonderland.client.hud.HUD;
import org.jdesktop.wonderland.client.hud.HUDComponent;
import org.jdesktop.wonderland.client.login.ServerSessionManager;
import org.jdesktop.wonderland.modules.isocial.client.ISocialManager;
import org.jdesktop.wonderland.modules.isocial.client.view.DockableSheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.ResultListener;
import org.jdesktop.wonderland.modules.isocial.client.view.SheetView;
import org.jdesktop.wonderland.modules.isocial.client.view.annotation.View;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.ResultMetadata;
import org.jdesktop.wonderland.modules.isocial.common.model.Role;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.tokensheet.common.TokenResult;
import org.jdesktop.wonderland.modules.isocial.tokensheet.common.TokenSheet;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManager;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerFactory;
import org.jdesktop.wonderland.modules.presencemanager.client.PresenceManagerListener;
import org.jdesktop.wonderland.modules.presencemanager.common.PresenceInfo;
import org.jdesktop.wonderland.modules.securitygroups.common.GroupDTO;
import org.jdesktop.wonderland.modules.securitygroups.common.GroupUtils;

/**
 * Token Guide View
 * @author Jonathan Kaplan <Jonathankap@wonderbuilders.com>
 * @author Ryan Babiuch
 * @author Kaustubh
 */
@View(value = TokenSheet.class, roles = {Role.GUIDE, Role.ADMIN})
public class TokenGuideView extends javax.swing.JPanel
        implements SheetView, ResultListener, PropertyChangeListener, PresenceManagerListener, DockableSheetView {

    private static final Logger LOGGER =
            Logger.getLogger(TokenGuideView.class.getName());
    private ISocialManager manager;
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(
            "org/jdesktop/wonderland/modules/isocial/tokensheet/client/Bundle");
    private Sheet sheet;
    private Role role;
    private TokenStudentViewPanel panel;
    private HashMap<String, StudentDetailsPanel> studentPanels;
    private HashMap<String, UserRecord> studentRecords;
    private int rows = 0;
    private PresenceManager pm;
    private List<String> students;
    private String strength;
    private JLabel topLabel;
    private JPanel topPanel;
    private URL audioSource;
    private AudioCacheHandler audioCacheHandler;
    private VolumeConverter volumeConverter;

    /** Creates new form TokenGuideView */
    public TokenGuideView() {
        initComponents();
    }

    public void initialize(ISocialManager manager, Sheet sheet, Role role) {
        this.manager = manager;
        this.sheet = sheet;
        this.role = role;
        studentPanels = new HashMap<String, StudentDetailsPanel>();
        studentRecords = new HashMap<String, UserRecord>();
        students = new ArrayList<String>();
        this.audioSource = getClass().getResource(BUNDLE.getString("audioSource"));
        audioCacheHandler = new AudioCacheHandler();
        volumeConverter = new VolumeConverter(0, 100);
        try {
            audioCacheHandler.initialize();
        } catch (AudioCacheHandlerException ex) {
            Logger.getLogger(TokenGuideView.class.getName()).log(Level.SEVERE, null, ex);
        }
        // listen for presence changes
        pm = PresenceManagerFactory.getPresenceManager(
                manager.getSession().getPrimarySession());

        //listen for results
        manager.addResultListener(sheet.getId(), this);
        // add any existing token results for existing users
        try {
            for (Result r : manager.getResults(sheet.getId())) {
                this.resultAdded(r);
            }
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, "Errror getting results", ioe);
        }
        topPanel = new JPanel(new GridBagLayout());
        topLabel = new JLabel("Class Students : " + rows);
        topLabel.setPreferredSize(new Dimension(300, 25));
        JButton refreshButton = new JButton(new ImageIcon(getClass().getResource("/org/jdesktop/wonderland/modules/isocial/tokensheet/client/resources/refresh_button.png")));
        topPanel.add(topLabel);
        topPanel.add(refreshButton);
        mainTab.add(topPanel);
        refreshButton.addActionListener(refreshListener);
        rows++;
        refreshButton.setPreferredSize(new Dimension(32, 32));
        mainTab.setPreferredSize(new Dimension(410, rows * (30) + 25));
        GridLayout gl = (GridLayout) mainTab.getLayout();
        gl.setRows(rows);
        addStudentPanels();
        pm.addPresenceManagerListener(this);
    }
    /*
     * The refresh button provides an ability for guides to check if there is new 
     * student in world. If yes, make sure his name is been shown on token system.
     */
    ActionListener refreshListener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            mainTab.removeAll();
            studentPanels.clear();
            rows = 0;
            rows++;
            topLabel.setText("Class Students : " + (rows - 1));
            mainTab.setPreferredSize(new Dimension(425, 25));
            repaint();
            mainTab.add(topPanel);
            mainTab.setPreferredSize(new Dimension(410, rows * (30) + 25));
            GridLayout gl = (GridLayout) mainTab.getLayout();
            gl.setRows(rows);
            addStudentPanels();
        }
    };

    public String getMenuName() {
        return ((TokenSheet) sheet.getDetails()).getName();
    }

    public boolean isAutoOpen() {
        return ((TokenSheet) sheet.getDetails()).isAutoOpen();
    }

    public HUDComponent open(HUD hud) {
        HUDComponent component = hud.createComponent(this);
        //component.setTransparency(1.0f);
        component.setPreferredLocation(Layout.NORTHEAST);
        return component;
    }

    public void close() {
        pm.removePresenceManagerListener(this);
        manager.removeResultListener(sheet.getId(), this);
    }

    public void resultAdded(final Result result) {
        if (result.getDetails() instanceof TokenResult) {
            TokenResult tResult = (TokenResult) result.getDetails();
            if (tResult.getType() == ResultType.TOKEN_INC) {
                TokenSoundPlayer.getInstance().playTokenSound();
            }
        }

        if (studentRecords.containsKey(result.getCreator())) {
            studentRecords.get(result.getCreator()).setResult(result);
        } else {
            UserRecord record = new UserRecord(result.getCreator());
            record.setResult(result);
            studentRecords.put(result.getCreator(), record);
        }
        if (studentPanels.containsKey(result.getCreator())) {
            studentPanels.get(result.getCreator()).updateResult(((TokenResult) result.getDetails()).getStudentResult());
        }
        //resultsModel.addElement(new NamedResult(result));
        //update the assigned tokens, passes, strikes asssigned from other OG.
    }

    public void resultUpdated(final Result result) {
        if (result.getDetails() instanceof TokenResult) {
            TokenResult tResult = (TokenResult) result.getDetails();
            if (tResult.getType() == ResultType.TOKEN_INC) {
                TokenSoundPlayer.getInstance().playTokenSound();
            }
        }
        
        if (studentRecords.containsKey(result.getCreator())) {
            studentRecords.get(result.getCreator()).setResult(result);
        } else {
            UserRecord record = new UserRecord(result.getCreator());
            record.setResult(result);
            studentRecords.put(result.getCreator(), record);
        }
        if (studentPanels.containsKey(result.getCreator())) {
            studentPanels.get(result.getCreator()).updateResult(((TokenResult) result.getDetails()).getStudentResult());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        mainTab = new javax.swing.JPanel();

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        mainTab.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        mainTab.setPreferredSize(new java.awt.Dimension(425, 25));
        mainTab.setLayout(new java.awt.GridLayout(0, 1));
        add(mainTab, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>                        
    // Variables declaration - do not modify                     
    private javax.swing.JPanel mainTab;
    // End of variables declaration                   

    public void presenceInfoChanged(PresenceInfo pi, ChangeType ct) {
        String username = pi.getUserID().getUsername();
        //do not create the student panel for self
        if (username.equals(manager.getUsername())) {
            return;
        }

        //do not create the student panel for non student members
        if (!isStudent(username)) {
            return;
        }

        StudentDetailsPanel panel1 = null;

        //if the changed user is student then check if it is ADDED or REMOVED
        if (ct == ChangeType.USER_ADDED) {
            if (studentPanels.containsKey(username)) {
                return;
            }
            Student student;
            UserRecord record;
            if (studentRecords.containsKey(username)) {
                record = studentRecords.get(username);
                student = ((TokenResult) record.getResult().getDetails()).getStudentResult();
            } else {
                student = new Student(username);
                record = new UserRecord(username);
                studentRecords.put(username, record);
            }
            rows++;
            GridLayout layout = (GridLayout) mainTab.getLayout();
            layout.setRows(rows);
            panel1 = new StudentDetailsPanel(manager, student, ((TokenSheet) sheet.getDetails()));
            panel1.addPropertyChangeListener(this);
            mainTab.add(panel1);
            studentPanels.put(username, panel1);
            Dimension pSize = panel1.getPreferredSize();
            mainTab.setPreferredSize(new Dimension(410, rows * (30) + 25));
            topLabel.setText("Class Students : " + (rows - 1));
            repaint();
        } else if (ct == ChangeType.USER_REMOVED) {
            rows--;
            GridLayout layout = (GridLayout) mainTab.getLayout();
            layout.setRows(rows);
            if (studentPanels.containsKey(username)) {
                panel1 = studentPanels.get(username);
                panel1.removePropertyChangeListener(this);
                mainTab.remove(panel1);
                studentPanels.remove(username);
                mainTab.setPreferredSize(new Dimension(410, rows * (30) + 25)); // 410 and 25 have been copied from StudentDetailsPanel.
                topLabel.setText("Class Students : " + (rows - 1));
                repaint();
            }
        }
    }

    private void addStudentPanels() {
        String myName = manager.getUsername();

        /**
         * Check the presence information for all the clients logged in. No action
         * for self. Get all the student names added on the guide token system 
         * panel.
         */
        PresenceInfo[] allUsers = pm.getAllUsers();
        Arrays.sort(allUsers, new Comparator<PresenceInfo>() {

            public int compare(PresenceInfo p1, PresenceInfo p2) {
                String user1 = p1.getUserID().getUsername();
                String user2 = p2.getUserID().getUsername();
                return Collator.getInstance().compare(user1, user2);
            }
        });
        for (int i = 0; i < allUsers.length; i++) {
            PresenceInfo user = allUsers[i];

            //for (String username : studentNames) {
            String username = user.getUserID().getUsername();
            if (username.equals(myName)) {
                continue;
            }

            if (!isStudent(username)) {
                continue;
            }

            if (studentPanels.containsKey(username)) {
                continue;
            }

            Student student = new Student(username);
            if (studentRecords.containsKey(username)) {
                Result result = studentRecords.get(username).getResult();
                if (result != null) {
                    student = ((TokenResult) studentRecords.get(username).getResult().
                            getDetails()).getStudentResult();
                } else {
                    studentRecords.remove(username);
                    studentRecords.put(username, new UserRecord(username));
                }
            } else {
                studentRecords.put(username, new UserRecord(username));
            }
            StudentDetailsPanel panel1 = new StudentDetailsPanel(manager, student, ((TokenSheet) sheet.getDetails()));
            panel1.addPropertyChangeListener(this);
            mainTab.add(panel1);
            rows++;
            GridLayout layout = (GridLayout) mainTab.getLayout();
            layout.setRows(rows);
            mainTab.setPreferredSize(new Dimension(410, rows * (30) + 25));
            studentPanels.put(username, panel1);
            topLabel.setText("Class Students : " + (rows - 1));
        }
    }

    public void propertyChange(PropertyChangeEvent pce) {
        String studentName = pce.getPropertyName();
        if (studentPanels.containsKey(studentName)) {
            StudentDetailsPanel studentPanel = studentPanels.get(studentName);
            UserRecord record = studentRecords.get(studentName);
            TokenResult details = studentPanel.getDetails();
            try {
                Result r = null;
                if (record.getResult() == null) {
                    // submit result with visibility set to public
                    ResultMetadata metadata = new ResultMetadata();
                    metadata.setVisibility(ResultMetadata.Visibility.PUBLIC);
                    r = manager.submitResultAs(record.getName(), sheet.getId(),
                            details, metadata);
                } else {
                    //details.getStudentResult().setTokensValue(totalTokens);
                    ResultMetadata metadata = new ResultMetadata();
                    metadata.setVisibility(ResultMetadata.Visibility.PUBLIC);
                    r = manager.updateResult(record.getResult().getId(), details, metadata);
                }
                record.setResult(r);
            } catch (IOException ex) {
                Logger.getLogger(TokenGuideView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean isDockable() {
        return ((TokenSheet) sheet.getDetails()).isDockable();
    }

    /**
     * Determine whether the given username belongs to "students" group.
     * @param username
     * @return
     */
    private boolean isStudent(String username) {
        ServerSessionManager session = manager.getSession();
        try {
            Set<GroupDTO> groups = GroupUtils.getGroupsForUser(session.getServerURL(),
                    username, false, session.getCredentialManager());
            for (GroupDTO group : groups) {
                if (group.getId().equals("students")) {
                    return true;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(TokenGuideView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(TokenGuideView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    class UserRecord {

        private final String name;
        private Result result;

        public UserRecord(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public Result getResult() {
            return result;
        }

        public void setResult(Result result) {
            this.result = result;
        }
    }
}
