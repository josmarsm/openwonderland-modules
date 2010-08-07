/**
 * Open Wonderland
 *
 * Copyright (c) 2010, Open Wonderland Foundation, All Rights Reserved
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

package org.jdesktop.wonderland.modules.programmingdemo.client.script;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import org.jdesktop.wonderland.modules.programmingdemo.client.script.ScriptVisualizer.Mode;
import org.jdesktop.wonderland.modules.programmingdemo.client.script.ScriptVisualizer.Status;
import org.jdesktop.wonderland.modules.programmingdemo.client.script.compiler.CharSequenceCompiler;
import org.jdesktop.wonderland.modules.programmingdemo.client.script.compiler.ClassScannerCache;

/**
 * Implementation of script controller
 * @author Jonathan Kaplan <jonathankap@gmail.com>
 */
public class ScriptManager implements ScriptControl {
    private static final Logger logger =
            Logger.getLogger(ScriptManager.class.getName());

    private final ScriptVisualizer visualizer;

    private final ExecutorService executor;
    private Future scriptRun;
    private Semaphore stepSemaphore;

    private Mode mode = Mode.RUN;
    private Status status = Status.STOPPED;

    private SoftReference<ClassScannerCache> cacheRef;

    public ScriptManager(ScriptVisualizer visualizer) {
        this.visualizer = visualizer;
        this.executor = Executors.newSingleThreadExecutor();

        cacheRef = new SoftReference<ClassScannerCache>(null);
    }

    public <T extends Script> T compileScript(String classname, String script,
                                              Class<T>... clazz)
    {
        logger.warning("Compile " + script);
        
        // get the shared cache
        ClassScannerCache cache = cacheRef.get();
        if (cache == null) {
            cache = new ClassScannerCache();
            cacheRef = new SoftReference<ClassScannerCache>(cache);
        }
        
        // create a new compiler to ensure no data (like compiled classes)
        // is saved between runs
        CharSequenceCompiler<T> compiler = 
                new CharSequenceCompiler<T>(getClass().getClassLoader(), null, cache);
        DiagnosticCollector<JavaFileObject> diag = new DiagnosticCollector<JavaFileObject>();

        try {                
            Class<T> c = compiler.compile(classname, script, diag, clazz);
            T out = c.newInstance();
            out.setScriptControl(this);
            return out;
        } catch (Exception ex) {
            StringBuffer msg = new StringBuffer("Compilation failed\n");

            for (Diagnostic<? extends JavaFileObject> d : diag.getDiagnostics()) {
                msg.append("Error at line " + d.getLineNumber() + ": " +
                           d.getMessage(Locale.getDefault()) + "\n");
                msg.append(script.substring((int) d.getStartPosition(),
                                            (int) d.getEndPosition()) + "\n");
            }

            logger.warning(msg.toString());

            return null;
        }
    }

    public void highlightLine(final int line) {
        // highlight the line in the editor
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                visualizer.highlightLine(line);
            }
        });
    }

    public void pause(final int... highlightIndices) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                visualizer.highlightObjects(highlightIndices);
            }
        });
        
        // determine if we need to pause
        try {
            switch (getMode()) {
                case RUN:
                    Thread.sleep(250);
                    break;
                case STEP:
                    setStatus(Status.PAUSED);
                    stepSemaphore.acquire();
                    setStatus(Status.RUNNING);
                    break;
                }
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }

    public void start() {
        setStatus(Status.RUNNING);
        setMode(Mode.RUN);
        checkStart();
    }

    public void stop() {
        Future f = getScriptRun();
        if (f != null) {
            f.cancel(true);
        }

        setScriptRun(null);
        visualizer.clearHighlight();
        visualizer.highlightObjects();
        setStatus(Status.STOPPED);
    }

    public void pause() {
        setMode(Mode.STEP);
    }

    public void step() {
        setMode(Mode.STEP);
        setStatus(Status.RUNNING);
        if (!checkStart()) {
            stepSemaphore.release();
        }
    }

    private synchronized boolean checkStart() {
        if (getScriptRun() != null) {
            // script is already running
            return false;
        }

        // start a new run
        setScriptRun(executor.submit(new Runnable() {
            public void run() {
                visualizer.runScript();
                setScriptRun(null);
                stop();
            }
        }));

        return true;
    }

    private synchronized void setScriptRun(Future f) {
        this.scriptRun = f;
    }

    private synchronized Future getScriptRun() {
        return scriptRun;
    }

    private synchronized Mode getMode() {
        return mode;
    }

    private synchronized void setMode(Mode mode) {
        if (this.mode == mode) {
            return;
        }

        this.mode = mode;

        if (mode == Mode.STEP) {
            stepSemaphore = new Semaphore(0);
        } else {
            stepSemaphore.release();
            stepSemaphore = null;
        }

        visualizer.setMode(mode);
    }

    private synchronized Status getStatus() {
        return status;
    }

    private synchronized void setStatus(Status status) {
        if (this.status == status) {
            return;
        }

        this.status = status;
        visualizer.setStatus(status);
    }

    public String rewrite(String scriptSnippet) {
        String[] lines = scriptSnippet.split("\n");

        boolean inComment = false;

        // go through the script, and pick out the import line numbers
        List<NumberedLine> numbered = new ArrayList<NumberedLine>();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            boolean highlight = false;

//            // ignore blank lines
//            if (line.isEmpty()) {
//                highlight = false;
//            }
//
//            // ignore comment lines
//            if (highlight && line.startsWith("//")) {
//                highlight = false;
//            }
//
//            // are we in a comment?
//            if (inComment) {
//                // look for comment ending
//                if (line.contains("*/")) {
//                    inComment = false;
//                }
//
//                highlight = false;
//            }
//
//            // are we at the start of a comment
//            if (highlight && line.startsWith("/*")) {
//                if (!line.contains("*/")) {
//                    inComment = true;
//                }
//                highlight = false;
//            }
//
//            // ignore braces on their own line
//            if (line.equals("{") || line.equals("}")) {
//                highlight = false;
//            }
//
//            // ignore return statements
//            if (line.equals("return;")) {
//                highlight = false;
//            }

            // add highlights to pause calls
            if (line.startsWith("pause(")) {
                highlight = true;
            }

            NumberedLine nl = new NumberedLine();
            nl.number = i;
            nl.line = line;
            nl.highlight = highlight;
            numbered.add(nl);
        }

        // now go throug and build the final script by adding the numbered
        // lines and some extra code to identify the line
        StringBuffer out = new StringBuffer();
        for (NumberedLine nl : numbered) {
            if (nl.highlight) {
                out.append("getScriptControl().highlightLine(" + nl.number + ");\n");
            }
            out.append(nl.line + "\n");
        }
        return out.toString();
    }

    private class NumberedLine {
        int number;
        String line;
        boolean highlight;
    }
}
