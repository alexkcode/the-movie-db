package com.moviedb.explorer;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utility {
    public static List<String> combinePageResults(
            Function<Integer, Supplier<List<String>>> function, int min, int max) {
        List<CompletableFuture<List<String>>> futures =
                IntStream.rangeClosed(min, max)
                        .boxed()
                        .map(page -> CompletableFuture.supplyAsync(
                                function.apply(page)))
                        .collect(Collectors.toList());

        Optional<List<String>> results = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .filter(a -> !a.isEmpty())
                .reduce((a, b) -> {
                    a.addAll(b);
                    return a;
                });

        return results.orElse(Collections.emptyList());
    }

    public static List<String> combinePageResultsParallel(
            Function<Integer, Supplier<List<String>>> function, int min, int max) {
        Optional<List<String>> results = IntStream.rangeClosed(min, max)
                .parallel()
                .boxed()
                .map(page -> function.apply(page).get())
                .filter(Objects::nonNull)
                .filter(a -> !a.isEmpty())
                .reduce((a, b) -> {
                    a.addAll(b);
                    return a;
                });

        return results.orElse(Collections.emptyList());
    }

}
