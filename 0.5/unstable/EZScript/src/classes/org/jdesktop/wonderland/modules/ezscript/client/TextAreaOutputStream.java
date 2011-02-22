/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdesktop.wonderland.modules.ezscript.client;

import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author jagwire
 */
public class TextAreaOutputStream extends OutputStream {
    private JTextArea textArea;
    public TextAreaOutputStream(JTextArea textArea) {
        super();
        this.textArea = textArea;
    }

    private void redirect(final String s) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String str = s;
                if(str.equals("\n")) {

                    str += "> ";
                } else {
                    str = "\n" + str;
                }
                textArea.append(str);
                textArea.setCaretPosition(textArea.getText().length());
            }
        });
    }

    @Override
    public void write(int i) throws IOException {
        redirect(String.valueOf((char)i));

    }
    
    @Override
    public void write(byte[] bytes, int offset, int length) {
        redirect(new String(bytes, offset, length));
    }
    @Override
    public void write(byte[] bytes) {
        write(bytes, 0, bytes.length);
    }
}
