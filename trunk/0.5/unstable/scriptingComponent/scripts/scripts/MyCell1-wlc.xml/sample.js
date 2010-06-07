var v = new java.lang.Runnable()
{
run: function()
  {
  print('Hello javascript world - from script   **** ' + mystring + ' \r\n') ;
  var x = prompt("Enter a string");
  MyClass.methodA(mystring);
  }
}
v.run();

