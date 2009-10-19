require "java"  
  
include_class %w{  
javax.swing.JFrame  
javax.swing.JPanel  
javax.swing.JButton  
java.awt.event.ActionListener  
}  
  
frame = JFrame.new("My New Window")  
frame.set_default_close_operation(JFrame::EXIT_ON_CLOSE)  
frame.set_size(200,200)  
  
panel = JPanel.new  
  
button = JButton.new("Click Me")  
  
listener = ActionListener.impl do  
puts "button was clicked"  
end  
  
button.add_action_listener(listener)  
  
panel.add(button)  
  
frame.add(panel)  
frame.pack()  
frame.show()  
