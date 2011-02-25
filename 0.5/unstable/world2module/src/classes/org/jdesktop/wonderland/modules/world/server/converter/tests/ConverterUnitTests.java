package org.jdesktop.wonderland.modules.world.server.converter.tests;

import java.awt.Dialog;
import java.awt.FileDialog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.jdesktop.wonderland.modules.world.server.converter.ModuleMetaData;
import org.jdesktop.wonderland.modules.world.server.converter.ServerMetaData;
import org.jdesktop.wonderland.modules.world.server.converter.SimpleModuleMetaData;
import org.jdesktop.wonderland.modules.world.server.converter.DefaultServerMetaData;
import org.jdesktop.wonderland.modules.world.server.converter.WorldToModuleConverter;
import org.jdesktop.wonderland.modules.world.common.xml.SAXParserDocumentDetailsRetriever;
import org.jdesktop.wonderland.modules.world.common.xml.XmlConversionHandler;

/**
 * Unit Tests for XML parsing / module creation from snapshot.
 *
 * //TODO And JUnit Annotations once JUnit is installed in project.
 *
 * @author Carl Jokl
 */
public class ConverterUnitTests {

    /**
     * This value should be set to the xml file which is to be used in unit testing.
     */
    public static final String UNIT_TEST_XML_TEST_FILE = "test.xml";

    /**
     * This location should be set to where the wonderland server folder is located.
     */
    public static final String UNIT_TEST_WONDERLAND_SERVER_DIR = "/home/wonderland/.wonderland-server/0.5-dev/";

    /**
     * This value should be sent to the name of a snapshot which is to be exported for the test.
     */
    public static final String UNIT_TEST_WORLD_TO_EXPORT = "test-snapshot";

    /**
     * This value shout be set to whether the previous (unit test world to export) is a snapshot.
     * If true then the world represents a snapshot and if false it represents a regular world. 
     */
    public static final boolean UNIT_TEST_WORLD_IS_SNAPSHOT = true;

    /**
     * This value should be set to the name of the module to be created as part of the export.
     */
    public static final String UNIT_TEST_MODULE_TO_CREATE_NAME = "test";

    /**
     * This value should be set to the description of the module to be created as part of the export.
     */
    public static final String UNIT_TEST_MODULE_TO_CREATE_DESCRIPTION = "snapshot to module export test";

    /**
     * This value should be set to the major version of the module to be created.
     */
    public static final int UNIT_TEST_MODULE_TO_CREATE_MAJOR_VERSION = 0;

    /**
     * This value should be set to the minor version of the module to be created.
     */
    public static final int UNIT_TEST_MODULE_TO_CREATE_MINOR_VERSION = 1;

    private String testXmlFile;
    private String wonderlandServerDir;
    private String exportWorldName;
    private boolean exportSnapshot;
    private String exportModuleName;
    private String exportModuleDesc;
    private int exportModuleMajorVer;
    private int exportModuleMinorVer;

    /**
     * Create a new instance of ConverterUnitTests using the values as specified in the static constants.
     */
    public ConverterUnitTests() {
        this(UNIT_TEST_XML_TEST_FILE,
             UNIT_TEST_WONDERLAND_SERVER_DIR,
             UNIT_TEST_WORLD_TO_EXPORT,
             UNIT_TEST_WORLD_IS_SNAPSHOT,
             UNIT_TEST_MODULE_TO_CREATE_NAME,
             UNIT_TEST_MODULE_TO_CREATE_DESCRIPTION,
             UNIT_TEST_MODULE_TO_CREATE_MAJOR_VERSION,
             UNIT_TEST_MODULE_TO_CREATE_MINOR_VERSION);
    }

    /**
     * Create a new instance of ConverterUnitTest using the specified values for testing.
     *
     * @param testXmlFile The test xml file name.
     * @param wonderlandServerDir The directory in which the wonderland server repository is located.
     * @param exportWorldName The name of the world to be exported.
     * @param exportSnapshot Whether the specified world to be exported is a snapshot. True if the world is a snapshot or false otherwise.
     * @param exportModuleName The name of a module into which the world is to be exported.
     * @param exportModuleDesc The description of the world module being created.
     * @param exportModuleMajorVer The major module version to use when exporting the module.
     * @param exportModuleMinorVer The minor module version number to use when exporting the module.
     */
    public ConverterUnitTests(String testXmlFile, String wonderlandServerDir, String exportWorldName, boolean exportSnapshot, String exportModuleName, String exportModuleDesc, int exportModuleMajorVer, int exportModuleMinorVer) {
        this.testXmlFile = testXmlFile;
        this.wonderlandServerDir = wonderlandServerDir;
        this.exportWorldName = exportWorldName;
        this.exportSnapshot = exportSnapshot;
        this.exportModuleName = exportModuleName;
        this.exportModuleDesc = exportModuleDesc;
        this.exportModuleMajorVer = exportModuleMajorVer;
        this.exportModuleMinorVer = exportModuleMinorVer;
    }

    /**
     * Create a new instance of ConverterUnitTest using the specified values for testing.
     *
     * @param wonderlandServerDir The directory in which the wonderland server repository is located.
     * @param exportWorldName The name of the world to be exported.
     * @param exportSnapshot Whether the specified world to be exported is a snapshot. True if the world is a snapshot or false otherwise.
     * @param exportModuleName The name of a module into which the world is to be exported.
     * @param exportModuleDesc The description of the world module being created.
     * @param exportModuleMajorVer The major module version to use when exporting the module.
     * @param exportModuleMinorVer The minor module version number to use when exporting the module.
     */
    public ConverterUnitTests(String wonderlandServerDir, String exportWorldName, boolean exportSnapshot, String exportModuleName, String exportModuleDesc, int exportModuleMajorVer, int exportModuleMinorVer) {
        this(null, wonderlandServerDir, exportWorldName, exportSnapshot, exportModuleName, exportModuleDesc, exportModuleMajorVer, exportModuleMinorVer);
    }

    /**
     * Test that an XML file can be parsed and written out again without adverse side effects.
     */
    public void testXMLConverson() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        {
            XMLStreamWriter echoWriter = null;
            try {
                File xmlFile = null;
                if (testXmlFile == null) {
                    FileDialog fileSelection = new FileDialog((Dialog) null, "Select an XML document to test with.");
                    fileSelection.setMode(FileDialog.LOAD);
                    fileSelection.setVisible(true);
                    String dirString = fileSelection.getDirectory();
                    String fileString = fileSelection.getFile();
                    if (dirString != null && !dirString.isEmpty() && fileString != null && !fileString.isEmpty()) {
                        xmlFile = new File(dirString, fileString);
                    }
                }
                else
                {
                    xmlFile = new File(testXmlFile);
                }
                if (xmlFile != null && xmlFile.exists()) {
                    SAXParser parser = factory.newSAXParser();
                    File destination = new File(xmlFile.getParentFile(), "modified-" + xmlFile.getName());
                    String encodingName = "UTF-8";
                    XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
                    echoWriter = xmlOutputFactory.createXMLStreamWriter(new OutputStreamWriter(new FileOutputStream(destination), encodingName));
                    DefaultHandler handler = new XmlConversionHandler(echoWriter, encodingName, new SAXParserDocumentDetailsRetriever(parser));
                    parser.setProperty(SAXParserDocumentDetailsRetriever.SAX_LEXICAL_HANDLER_PROPERTY_NAME, handler);
                    parser.parse(xmlFile, handler);
                    echoWriter.flush();
                }
            }
            catch (XMLStreamException xse) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failure to create XML Output writer!", xse);
            }
            catch (IOException ioe) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "IO Error while parsing/writing", ioe);
            }
            catch (ParserConfigurationException pce) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error configuring parser!", pce);
            }
            catch (SAXException se) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error creatng parser!", se);
            }
            finally {
                try {
                    if (echoWriter != null) {
                        echoWriter.close();
                    }
                }
                catch (XMLStreamException xse) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed to close destination", xse);
                }
            }
        }
    }

    /**
     * Test the full process of automatic creation of a module.
     */
    public void testModuleCreation() {
        ModuleMetaData moduleMetaData = new SimpleModuleMetaData(exportModuleName, exportModuleDesc, exportModuleMajorVer, exportModuleMinorVer);
        ServerMetaData serverMetaData = new DefaultServerMetaData(wonderlandServerDir);
        WorldToModuleConverter converter = new WorldToModuleConverter();
        File moduleArchive = converter.convertSnapshotToModule(exportWorldName, exportSnapshot, moduleMetaData, serverMetaData);
        //ToDo: Unit test module archive was created.
    }
}
