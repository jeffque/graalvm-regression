package com.jeffque;

import org.graalvm.polyglot.Context;

public class App {

    public static void main(String[] args) {
         final var context = Context.newBuilder()
                .allowExperimentalOptions(true)
                .option("js.nashorn-compat", "true")
                .option("js.ecmascript-version", "2020")
                .allowAllAccess(true)
                .build();

        final var script =
        """
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
        """;
        final var contextEval = context.eval("js", script);

        System.out.println(contextEval.toString());
    }
}
