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
        var namesList = java.util.List.of("test","test1");
        
        namesList.stream()
        .findFirst()
        .map(s => java.util.stream.Stream.of(s.split(' '))
            .anyMatch(k => k.equals('test'))
        )
        """;
        final var contextEval = context.eval("js", script);

        System.out.println(contextEval.toString());
    }
}
