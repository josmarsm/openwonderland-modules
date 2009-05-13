from javax.swing import *
from java.awt import *
from javax.swing.table import DefaultTableModel
from javax.swing import JFrame
from java.awt import BorderLayout
from javax.swing import WindowConstants
from javax.swing import JTable
from javax.swing import JScrollPane
from java.awt import Dimension
from javax.swing import JPanel
from javax.swing import JLabel
from javax.swing import JButton

import sys

print 'Hello from jython - mouse script' ;
print name;
MyClass.testMethod("rabbit");
stateString[0] = "abracadabra";
name = "fubar";
print name;
print stateString[0];
stateString[0] = "snapz";
print stateString[0];
testInt = 1006;

class Example:

  def myExit(self, event):
      sys.dispose();

  def setText(self,event):
      self.label.text = self.table.getValueAt(0, 2)
      eventScriptType[0] = self.table.getValueAt(0, 2);
      eventNames[0] = self.table.getValueAt(0, 1);

  def __init__(self):
    frame = JFrame("Jython JTable Example")
    frame.setSize(500, 250)
    frame.setLayout(BorderLayout())

    self.tableData = [
      ['Mouse 1', eventNames[0], eventScriptType[0]],
      ['Mouse 2', eventNames[1] , eventScriptType[1]],
      ['Mouse 3', eventNames[2], eventScriptType[2]],
      ['Mouse 1 Shift', eventNames[3], eventScriptType[3]],
      ['Mouse 2 Shift',eventNames[4], eventScriptType[4]],
      ['Mouse 3 Shift',eventNames[5], eventScriptType[5]],
      ['Mouse 1 Control',eventNames[6], eventScriptType[6]],
      ['Mouse 2 Control',eventNames[7], eventScriptType[7]],
      ['Mouse 3 Control',eventNames[8], eventScriptType[8]],
       ]
    colNames = ('Script/Event','Name','Type')
    dataModel = DefaultTableModel(self.tableData, colNames)
    self.table = JTable(dataModel)

    scrollPane = JScrollPane()
    scrollPane.setPreferredSize(Dimension(400,200))
    scrollPane.getViewport().setView((self.table))

    panel = JPanel()
    panel.add(scrollPane)

    frame.add(panel, BorderLayout.CENTER)

    self.label = JLabel('Hello from Jython')
    frame.add(self.label, BorderLayout.NORTH)
    button = JButton('Save Settings',actionPerformed=self.setText)
    frame.add(button, BorderLayout.SOUTH)
    exitButton = JButton('Exit',actionPerformed=self.myExit)
    frame.add(exitButton, BorderLayout.EAST)

    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
    frame.setVisible(True)

Example()

