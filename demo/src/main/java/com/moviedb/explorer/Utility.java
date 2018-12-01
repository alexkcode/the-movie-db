package com.moviedb.explorer;

import com.nurkiewicz.asyncretry.RetryExecutor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utility {
    public static <T> List<String> combinePageResults(
            Function<T, Supplier<List<String>>> function, RetryExecutor executor,
            Stream<T> boxed) {

        List<CompletableFuture<List<String>>> futures = boxed
                // apply supplier function with retry logic
                .map(element -> executor.getFutureWithRetry(retryCallable -> CompletableFuture.supplyAsync(
                        function.apply(element))))
                .collect(Collectors.toList());

        Optional<List<String>> results = futures.stream()
                .map(CompletableFuture::join)
//                .filter(Objects::nonNull)
//                .filter(a -> !a.isEmpty())
                .reduce((a, b) -> {
                    a.addAll(b);
                    return a;
                });

        return results.orElse(Collections.emptyList());
    }

    public static <T> List<String> combinePageResultsParallel(
            Function<T, Supplier<List<String>>> function, Stream<T> boxed) {
        Optional<List<String>> results = boxed
                .parallel()
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
