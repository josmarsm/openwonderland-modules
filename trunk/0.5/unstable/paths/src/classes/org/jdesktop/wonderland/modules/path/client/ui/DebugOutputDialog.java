package org.jdesktop.wonderland.modules.path.client.ui;

import java.util.ArrayDeque;
import java.util.Queue;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.jdesktop.wonderland.modules.path.common.Disposable;

/**
 * This is a dialog for outputting debug information.
 *
 * @author Carl Jokl
 */
public class DebugOutputDialog extends JFrame {

    /**
     * Get and show the DebugOutputDialog.
     *
     * @return The created DebugOutputDialog.
     */
    public static DebugOutputDialog createDialog() {
        DebugDialogCreator creator = new DebugDialogCreator();
        DebugOutputDialog dialog = creator.getDialog();
        SwingUtilities.invokeLater(creator);
        return dialog;
    }

    private TextAppender textAppender;

    /**
     * Create a new instance of the DebugOutputDialog with the specified
     * Window as the owner. This constructor is private so that creation
     * can be done on the UI thread.
     *
     */
    private DebugOutputDialog() {
        super("Debug Dialog");
        JTextArea displayText = new JTextArea();
        this.getContentPane().add(new JScrollPane(displayText));
        textAppender = new TextAppender(displayText);
    }

    /**
     * Append the specified text to the debug output.
     *
     * @param value A String containing the text to be appended.
     */
    public void append(String value) {
        textAppender.appendText(value);
        SwingUtilities.invokeLater(textAppender);
    }

    /**
     * Append the specified text to the debug output and drop to a
     * new line afterward.
     *
     * @param value A String containing the text to be appended. A newline
     *              will be added afterward.
     */
    public void appendln(String value) {
        append(String.format("%s\n", value));
    }

    /**
     * Class used to create a DebugOutputDialog on the UI thread.
     */
    private static class DebugDialogCreator implements Runnable, Disposable {

        private DebugOutputDialog dialog;

        /**
         * Create a new instance of a DebugDialogCreator to create
         * a DebugOutputDialog on the UI thread.
         */
        public DebugDialogCreator() {
            dialog = new DebugOutputDialog();
        }

        /**
         * Create a new DebugOutputDialog on the UI thread.
         */
        @Override
        public void run() {
            dialog.setSize(400, 300);
            dialog.setVisible(true);
            dialog = null;
        }

        /**
         * Get the current DebugOutputDialog. This should be called
         * after the DebugOutputDialog has had chance to be created.
         *
         * @return The DebugOutputDialog created by this DebugDialogCreator
         *         if created or null otherwise.
         */
        public DebugOutputDialog getDialog() {
            return dialog;
        }

        /**
         * Clear any references to other objects to aid garbage collection.
         */
        @Override
        public void dispose() {
            dialog = null;
        }
    }

    /**
     * Runnable class to append text to a JTextArea on the UI thread.
     */
    private static class TextAppender implements Runnable {

        private JTextArea destination;
        private Queue<String> pendingTextItems;

        /**
         * Create a new TextAppender to append text to the
         * specified JTextArea.
         *
         * @param destination The destination JTextArea to which
         *                    text will be appended.
         */
        public TextAppender(JTextArea destination) {
            this.destination = destination;
            pendingTextItems = new ArrayDeque<String>();
        }

        /**
         * Append a new piece of pending text to be appended on the UI thread.
         *
         * @param pendingText A String containing the pending text to be appended
         *                    on the UI thread.
         */
        public synchronized void appendText(String pendingText) {
            if (pendingText != null) {
                pendingTextItems.offer(pendingText);
            }
        }

        /**
         * Append any pending text on the UI thread.
         */
        @Override
        public synchronized void run() {
            if (!pendingTextItems.isEmpty()) {
                while (!pendingTextItems.isEmpty()) {
                    destination.append(pendingTextItems.poll());
                }
            }
        }
    }
}
