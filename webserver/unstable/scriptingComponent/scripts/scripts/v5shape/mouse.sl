[[System out] println: "Hello world!!!"];
[$MyClass testMethod: "Rabbit punch"];
[$MyClass setStateString: 0 "hoopie"];
println('hello world');
$v = $stateString[0];
println($v);
println('after @');
println($stateString[0]);
$stateString[0] = "This is a test";
println($stateString[0]);
setField($stateString[0] => "oops");
println($stateString[0]);
$l = [$MyClass getStateString: 0];
println($l);

