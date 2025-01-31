Reproducing issue https://github.com/oracle/graaljs/issues/886 as a standalone maven project.

## How to reproduce

To run with graalvm-24, version that the bug was reported:

```bash
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
./mvnw compile exec:java -P graalvm-20
```

It will print:

```none
Optional[true]
```
