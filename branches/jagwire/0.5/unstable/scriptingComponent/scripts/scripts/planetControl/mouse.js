
var swingNames = JavaImporter();

swingNames.importPackage(Packages.javax.swing);
swingNames.importPackage(Packages.java.awt);
swingNames.importPackage(Packages.java.awt.event);

function createComponents() 
{
    with (swingNames) {
	var buttonStart = new JButton("Start Solar System");
	var buttonStop = new JButton("Stop Solar System");
	buttonStart.addActionListener(function() 
	    {
	    CommThread.sendSocketMessage('101,start');
	    });
	buttonStop.addActionListener(function() 
	    {
	    CommThread.sendSocketMessage('101,stop');
	    });
/*
	buttonQuit.addActionListener(function() 
	    {
            java.lang.System.exit(0);
	    });
*/
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
	pane.add(new JLabel("Start "));
        pane.add(buttonStart);
	pane.add(new JLabel("Stop  "));
        pane.add(buttonStop);
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



