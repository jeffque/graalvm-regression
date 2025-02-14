Reproducing issue https://github.com/oracle/graaljs/issues/886 as a standalone maven project.

## How to reproduce

> this must be run at the branch [`reproduce-split-misbehavior`](https://github.com/jeffque/graalvm-regression/tree/reproduce-split-misbehavior)
> 
> Be sure to `git switch reproduce-split-misbehavior` before runing this code

To run with graalvm-24, version that the bug was reported:

```bash
# be sure the branch is `reproduce-split-misbehavior`
./mvnw compile exec:java -P graalvm-24
```

It will yield the error:

```none
Exception in thread "main" TypeError: k.equals is not a function
	at <js> :=>(Unnamed:6:154-169)
	at com.oracle.truffle.polyglot.PolyglotFunctionProxyHandler.invoke(PolyglotFunctionProxyHandler.java:151)
```

To run the previous known version with no error (graalvm-20):

```bash
# be sure the branch is `reproduce-split-misbehavior`
./mvnw compile exec:java -P graalvm-20
```

It will print:

```none
Optional[true]
```


## Proposed fix

The issue has been answered https://github.com/oracle/graaljs/issues/886#issuecomment-2630920074. By the answer, I concluded that it was not something configurable at runtime, that there must a change in code whatsoever.

So, to address this, I propose to add a code in the middle to ensure creating a more JS-ish code. This will ensure that the produced array is indeed js-array compatible.

```js
function as_js_array(arr) {
	// check if it is a java obj by probing the `equals` method existence
	if (!arr.equals) {
		// does not exist, return it
		console.debug("not java")
		return arr;
	}
	console.debug("yep, this is java, transforming to js array")
	// ok, it is java after all
	// assuming it was a java array, this can be checked further to be sure
	return java.util.stream.Stream.of(arr)
		.reduce([], (acc, el) => [...acc, el]);
}
```

With this function, let's run this sample?

```js
function as_js_array(arr) {
	// check if it is a java obj by probing the `equals` method existence
	if (!arr.equals) {
		// does not exist, return it
		console.debug("not java")
		return arr;
	}
	console.debug("yep, this is java, transforming to js array")
	// ok, it is java after all
	// assuming it was a java array, this can be checked further to be sure
	return java.util.stream.Stream.of(arr)
		.reduce([], (acc, el) => [...acc, el]);
}


// let us test if indeed the function detects java array
var ArrayJava = Java.type('java.lang.String[]')

var javaArray = new ArrayJava(3);
javaArray[0] = 'a'
javaArray[1] = 'b'
javaArray[2] = 'c'

var jsArray = as_js_array(javaArray)
console.log(jsArray)
console.log(jsArray.reduce((acc, el) => acc + el, ""))

// let us test if indeed the function detects js array
var nativeJsArray = [ 'a', 'b', 'c' ]
var js2JsArray = as_js_array([ 'a', 'b', 'c' ])
console.log(js2JsArray)
console.log(js2JsArray.reduce((acc, el) => acc + el, ""))

// original code, only changing the `Stream.of(s.split)` to  `as_js_array(s.split)`
// and adapting the API, some `instead` of `filter`
var namesList = java.util.List.of("te test st","test1");
namesList.stream()
	.findFirst()
	.map(s => as_js_array(s.split(' ')).some(el => el === 'test'))
```

The sample log output (ie, ignoring `console.debug`):

```txt
a,b,c
abc
a,b,c
abc
Optional[true]
```

in both profiles `-Pgraalvm20` and `-Pgraalvm24`. The `console.debug` will allow one to easily identify when the array was treated as a JS array or as a Java array. The full output for `-Pgraalvm20`:

```txt
yep, this is java, transforming to js array
a,b,c
abc
not java
a,b,c
abc
not java
Optional[true]
```

The full output for `-Pgraalvm24`:

```txt
yep, this is java, transforming to js array
a,b,c
abc
not java
a,b,c
abc
not java
Optional[true]
```
