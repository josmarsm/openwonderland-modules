/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Rhino code, released May 6, 1999.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1997-1999
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU General Public License Version 2 or later (the "GPL"), in which
 * case the provisions of the GPL are applicable instead of those above. If
 * you wish to allow use of your version of this file only under the terms of
 * the GPL and not to allow others to use your version of this file under the
 * MPL, indicate your decision by deleting the provisions above and replacing
 * them with the notice and other provisions required by the GPL. If you do
 * not delete the provisions above, a recipient may use your version of this
 * file under either the MPL or the GPL.
 *
 * ***** END LICENSE BLOCK ***** */

/*
 * SwingApplication.js - a translation into JavaScript of
 * SwingApplication.java, a java.sun.com Swing example.
 *
 * @author Roger E Critchlow, Jr.
 */

var swingNames = JavaImporter();

swingNames.importPackage(Packages.javax.swing);
swingNames.importPackage(Packages.java.awt);
swingNames.importPackage(Packages.java.awt.event);

function createComponents() 
{
    with (swingNames) {
	var textx = new JTextField("0.1");
	var texty = new JTextField("0.1");
	var textz = new JTextField("0.1");
	var textRot = new JTextField("0.01");
	var buttonMoveX = new JButton("Move X");
	var buttonMoveY = new JButton("Move Y");
	var buttonMoveZ = new JButton("Move Z");
	var buttonMoveTotal = new JButton("Move Total");
	var buttonRestore = new JButton("Restore Original Position");
	var buttonQuit = new JButton("Quit");
        // Since Rhino 1.5R5 JS functions can be passed to Java method if
        // corresponding argument type is Java interface with single method
        // or all its methods have the same number of arguments and the
        // corresponding arguments has the same type. See also comments for
        // frame.addWindowListener bellow
	buttonMoveX.addActionListener(function() 
	    {
	    var x = textx.getText();
	    MyClass.doTransform(x, 0, 0, 0);
	    });
	buttonMoveY.addActionListener(function() 
	    {
	    var y = texty.getText();
	    MyClass.doTransform(0, y, 0, 0);
	    });
	buttonMoveZ.addActionListener(function() 
	    {
	    var z = textz.getText();
	    MyClass.doTransform(0, 0, z, 0);
	    });
	buttonMoveTotal.addActionListener(function() 
	    {
	    var x = textx.getText();
	    var y = texty.getText();
	    var z = textz.getText();
	    var rot = textRot.getText();
	    MyClass.doTransform(x, y, z, rot);
	    });
	buttonRestore.addActionListener(function() 
	    {
	    MyClass.restoreRest();
	    });
	buttonQuit.addActionListener(function() 
	    {
            java.lang.System.exit(0);
	    });

        /*
         * An easy way to put space between a top-level container
         * and its contents is to put the contents in a JPanel
         * that has an "empty" border.
         */
        var pane = new JPanel();
        pane.border = BorderFactory.createEmptyBorder(30, //top
                                                      30, //left
                                                      10, //bottom
                                                      30); //right
        pane.setLayout(new GridLayout(7, 2));
	pane.add(new JLabel("      X"));
	pane.add(textx);
	pane.add(new JLabel("      Y"));
	pane.add(texty);
	pane.add(new JLabel("      Z"));
	pane.add(textz);
	pane.add(new JLabel("    Rot"));
	pane.add(textRot);

        pane.add(buttonMoveX);
        pane.add(buttonMoveY);
        pane.add(buttonMoveZ);
        pane.add(buttonMoveTotal);
	pane.add(buttonRestore);
/*	pane.add(buttonQuit); */

        return pane;
    }
}

with (swingNames) {
    try {
	UIManager.
            setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    } catch (e) { }

    //Create the top-level container and add contents to it.
    var frame = new swingNames.JFrame("SwingApplication");
    frame.getContentPane().add(createComponents(), BorderLayout.CENTER);

    // Pass JS function as implementation of WindowListener. It is allowed since 
    // all methods in WindowListener have the same signature. To distinguish 
    // between methods Rhino passes to JS function the name of corresponding 
    // method as the last argument  
    frame.addWindowListener(function(event, methodName) {
	if (methodName == "windowClosing") {     
            java.lang.System.exit(0);
	}
    });

    //Finish setting up the frame, and show it.
    frame.pack();
    frame.setVisible(true);
}



