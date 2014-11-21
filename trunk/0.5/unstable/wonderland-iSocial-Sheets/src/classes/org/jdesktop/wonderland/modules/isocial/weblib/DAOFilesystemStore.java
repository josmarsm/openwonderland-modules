/**
 * iSocial Project
 * http://isocial.missouri.edu
 *
 * Copyright (c) 2011, University of Missouri iSocial Project, All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * The iSocial project designates this particular file as
 * subject to the "Classpath" exception as provided by the iSocial
 * project in the License file that accompanied this code.
 */
package org.jdesktop.wonderland.modules.isocial.weblib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.jdesktop.wonderland.modules.isocial.common.model.Cohort;
import org.jdesktop.wonderland.modules.isocial.common.model.CohortState;
import org.jdesktop.wonderland.modules.isocial.common.model.ISocialModelBase;
import org.jdesktop.wonderland.modules.isocial.common.model.Instance;
import org.jdesktop.wonderland.modules.isocial.common.model.Lesson;
import org.jdesktop.wonderland.modules.isocial.common.model.Result;
import org.jdesktop.wonderland.modules.isocial.common.model.Sheet;
import org.jdesktop.wonderland.modules.isocial.common.model.Unit;
import org.jdesktop.wonderland.utils.RunUtil;

/**
 * Store contents of a DAO in the filesystem
 * @author Jonathan Kaplan <jonathankap@wonderbuilders.com>
 */
public class DAOFilesystemStore implements ISocialDAOListener {
    private static final Logger LOGGER =
            Logger.getLogger(DAOFilesystemStore.class.getName());

    private static final String DIRECTORY_PROP =
            "org.jdesktop.wonderland.modules.isocial.weblib.DAOFilesystemStore.directory";
    private static final String CURRENT_INSTANCE_FILE = "currentInstance";
    private static final File BASE_DIR;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    static {
        String dir = System.getProperty(DIRECTORY_PROP);
        if (dir == null) {
            BASE_DIR = new File(RunUtil.getRunDir(), "isocial-sheets");
        } else {
            BASE_DIR = new File(dir);
        }

        if (!BASE_DIR.exists() && !BASE_DIR.mkdirs()) {
            throw new RuntimeException("Unable to open backing directory " +
                                       BASE_DIR);
        }
    }

    public void readAll(DefaultDAOImpl dao) throws IOException {
        List<BackingFile> roots = new LinkedList<BackingFile>();

        for (File dir : BASE_DIR.listFiles()) {
            if (dir.isDirectory() && dir.list().length > 0) {
                BackingFile file = new BackingFile(dir);
                roots.add(file);
            }
        }

        for (BackingFile root : roots) {
            read(root, dao);
        }

        // read current instance
        File currentInst = new File(BASE_DIR, CURRENT_INSTANCE_FILE);
        if (currentInst.exists() && currentInst.isFile()) {
            Properties ciProps = new Properties();
            ciProps.load(new FileInputStream(currentInst));

            String currentInstanceId = ciProps.getProperty(CURRENT_INSTANCE_FILE);
            if (currentInstanceId != null &&
                dao.getInstance(currentInstanceId) != null)
            {
                dao.setCurrentInstanceInternal(currentInstanceId);
            }
        }
    }

    public void added(ISocialModelBase obj) {
        executor.execute(new AddOperation(obj));
    }

    public void updated(ISocialModelBase oldObj, ISocialModelBase newObj) {
        executor.execute(new UpdateOperation(oldObj, newObj));
    }

    public void removed(ISocialModelBase obj) {
        executor.execute(new RemoveOperation(obj));
    }

    public void currentInstanceChanged(String instanceId) {
        executor.execute(new CurrentInstanceOperation(instanceId));
    }

    public void shutdown() {
        executor.shutdown();
    }

    private BackingFile createBackingFile(ISocialModelBase obj)
        throws IOException
    {
        File dir = getDirectory(obj);
        if (dir.exists() &&
                (!dir.isDirectory() || dir.list().length > 0))
        {
            throw new IOException("Directory " + dir + " exists");
        }

        dir.mkdirs();
        return new BackingFile(dir);
    }

    private BackingFile getBackingFile(ISocialModelBase obj)
        throws IOException
    {
        File dir = getDirectory(obj);
        if (!dir.exists() || !dir.isDirectory() || dir.list().length == 0) {
            throw new IOException("Directory " + dir + " not valid.");
        }

        return new BackingFile(dir);
    }

    private void read(BackingFile file, DefaultDAOImpl dao) {
        try {
            ISocialModelBase obj = file.read();

            if (obj instanceof Cohort) {
                dao.addCohortInternal((Cohort) obj);
            } else if (obj instanceof Unit) {
                dao.addUnitInternal((Unit) obj);
            } else if (obj instanceof Lesson) {
                dao.addLessonInternal((Lesson) obj);
            } else if (obj instanceof Sheet) {
                dao.addSheetInternal((Sheet) obj);
            } else if (obj instanceof Instance) {
                dao.addInstanceInternal((Instance) obj);
            } else if (obj instanceof Result) {
                dao.addResultInternal((Result) obj);
            } else if (obj instanceof CohortState) {
                dao.addCohortStateInternal((CohortState) obj);
            } else {
                throw new IOException("Unknown type: " + obj + " " + obj.getClass());
            }

            for (BackingFile child : file.getChildren()) {
                read(child, dao);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error reading " + file.getName(), ex);
        }
    }

    private File getDirectory(ISocialModelBase obj) throws IOException {
        if (obj instanceof Cohort) {
            return getDirectory((Cohort) obj);
        } else if (obj instanceof Unit) {
            return getDirectory((Unit) obj);
        } else if (obj instanceof Lesson) {
            return getDirectory((Lesson) obj);
        } else if (obj instanceof Sheet) {
            return getDirectory((Sheet) obj);
        } else if (obj instanceof Instance) {
            return getDirectory((Instance) obj);
        } else if (obj instanceof Result) {
            return getDirectory((Result) obj);
        } else if (obj instanceof CohortState) {
            return getDirectory((CohortState) obj);
        } else {
            throw new IOException("Unknown type: " + obj + " " + obj.getClass());
        }
    }

    private File getDirectory(Cohort cohort) {
        return new File(BASE_DIR, safeName(cohort.getId(), cohort.getName()));
    }

    private File getDirectory(Unit unit) {
        return new File(BASE_DIR, safeName(unit.getId(), unit.getName()));
    }

    private File getDirectory(Lesson lesson) {
        Unit unit = dao().getUnit(lesson.getUnitId());
        if (unit == null) {
            throw new RuntimeException("Unknown unit " + lesson.getUnitId() +
                                       " for lesson " + lesson);
        }

        return new File(getDirectory(unit), safeName(lesson.getId(), lesson.getName()));
    }

    private File getDirectory(Sheet sheet) {
        Lesson lesson = dao().getLesson(sheet.getUnitId(), sheet.getLessonId());
        if (lesson == null) {
            throw new RuntimeException("Unknown lesson " + sheet.getLessonId() +
                                       " for sheet " + sheet);
        }

        return new File(getDirectory(lesson), safeName(sheet.getId(), sheet.getName()));
    }

    private File getDirectory(Instance instance) {
        String name = instance.getCohortId() + "-" +
                      DateFormat.getDateInstance().format(instance.getCreated());
        return new File(BASE_DIR, safeName(instance.getId(), name));
    }

    private File getDirectory(Result result) {
        Instance instance = dao().getInstance(result.getInstanceId());
        if (instance == null) {
            throw new RuntimeException("Unknown instance " + result.getInstanceId() +
                                       " for result " + result);
        }

        return new File(getDirectory(instance), safeName(result.getId(), result.getCreator()));
    }
    
    private File getDirectory(CohortState state) {
        Cohort cohort = dao().getCohort(state.getCohortId());
        if (cohort == null) {
            throw new RuntimeException("Unknown cohort " + state.getCohortId() +
                                       " for cohort state " + state);
        }
        
        return new File(getDirectory(cohort), safeString(state.getKey()));
    }

    private String safeName(String id, String name) {
        name = safeString(name);
        return id + "-" + name;
    }
    
    private static String safeString(String str) {
        return str.replaceAll("\\W", "_");
    }

    private static ISocialDAO dao() {
        return ISocialDAOFactory.getReadOnlyInstance();
    }

    class AddOperation implements Runnable {
        private final ISocialModelBase obj;

        public AddOperation(ISocialModelBase obj) {
            this.obj = obj;
        }

        public void run() {
            try {
                BackingFile bf = createBackingFile(obj);
                bf.write(obj);
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error creating backing file for " +
                           obj, ioe);
            }
        }
    }

    class UpdateOperation implements Runnable {
        private final ISocialModelBase oldObj;
        private final ISocialModelBase newObj;

        public UpdateOperation(ISocialModelBase oldObj,
                               ISocialModelBase newObj)
        {
            this.oldObj = oldObj;
            this.newObj = newObj;
        }

        public void run() {
            try {
                LOGGER.warning("Get directory for old : " + oldObj + ": " +
                               getDirectory(oldObj));

                BackingFile backing = getBackingFile(oldObj);
                if (backing == null) {
                    throw new IOException("Backing file not found for " + oldObj);
                }

                LOGGER.warning("Get directory for new : " + newObj + ": " +
                               getDirectory(newObj));

                // check if the directory name has changed
                File dir = getDirectory(newObj);
                if (!dir.equals(backing.getDirectory())) {
                    backing = backing.moveTo(dir);
                }

                backing.write(newObj);
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error updating backing file for " +
                           newObj, ioe);
            }
        }
    }

    class RemoveOperation implements Runnable {
        private final ISocialModelBase obj;

        public RemoveOperation(ISocialModelBase obj) {
            this.obj = obj;
        }

        public void run() {
            try {
                BackingFile backing = getBackingFile(obj);
                if (backing != null) {
                    backing.remove();
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error removing backing file for " +
                           obj, ioe);
            }
        }
    }

    class CurrentInstanceOperation implements Runnable {
        private final String instanceId;

        public CurrentInstanceOperation(String instanceId) {
            this.instanceId = instanceId;
        }

        public void run() {
            try {
                // write the current instance
                Properties ciProps = new Properties();
                ciProps.setProperty(CURRENT_INSTANCE_FILE, instanceId);

                File currentInst = new File(BASE_DIR, CURRENT_INSTANCE_FILE);
                ciProps.store(new FileOutputStream(currentInst), null);
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "Error writing current instance", ioe);
            }
        }
    }

    private static class BackingFile {
        private static final JAXBContext CONTEXT;
        static {
            try {
                Collection<Class> types = ISocialWebUtils.getISocialModelTypes();
                CONTEXT = JAXBContext.newInstance(types.toArray(new Class[0]));
            } catch (JAXBException jex) {
                throw new RuntimeException("Error creating context", jex);
            }
        }

        private final File directory;

        BackingFile(File directory) {
            this.directory = directory;
        }

        File getDirectory() {
            return directory;
        }

        String getName() {
            return directory.getName();
        }

        File getDataFile() {
            return new File(directory, getName());
        }

        List<BackingFile> getChildren() {
            List<BackingFile> out = new ArrayList<BackingFile>();

            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    out.add(new BackingFile(file));
                }
            }

            return out;
        }

        ISocialModelBase read() throws IOException {            
            try {
                Unmarshaller u = CONTEXT.createUnmarshaller();
                return (ISocialModelBase) u.unmarshal(getDataFile());
            } catch (JAXBException je) {
                throw new IOException(je);
            }
        }

        void write(ISocialModelBase obj) throws IOException {
            try {
                directory.mkdirs();

                Marshaller m = CONTEXT.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                m.marshal(obj, getDataFile());
            } catch (JAXBException je) {
                throw new IOException(je);
            }
        }

        void remove() throws IOException {
            // recursively remove children
            for (BackingFile child : getChildren()) {
                child.remove();
            }

            getDataFile().delete();
            directory.delete();
        }

        BackingFile moveTo(File newDir) {
            // record where the data file used to be
            String oldName = getName();

            LOGGER.warning("Old name: " + oldName);

            // rename the directory
            directory.renameTo(newDir);

            BackingFile newFile = new BackingFile(newDir);
            
            LOGGER.warning("After rename: " + newFile.getName());

            // get the old location of the data file
            File oldData = new File(newDir, oldName);

            LOGGER.warning("Old data: " + oldData);

            // get the current location of the data file
            File newData = newFile.getDataFile();

            LOGGER.warning("New data: " + newData);

            // get the
            oldData.renameTo(newData);

            return newFile;
        }
    }
}
