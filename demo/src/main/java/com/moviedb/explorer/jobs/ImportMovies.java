package com.moviedb.explorer.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

public class ImportMovies {

    private final String path;
    private ObjectMapper objectMapper;

    @Value("${url}")
    private String server;

    @Autowired
    private RestTemplate rest;

    private HttpHeaders headers;

    @Value("${api_key}")
    private String apiKey;

    public ConcurrentLinkedQueue<IOException> getExceptions() {
        return exceptions;
    }

    private ConcurrentLinkedQueue<IOException> exceptions;

    public ImportMovies() {
        objectMapper = new ObjectMapper();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");

        exceptions = new ConcurrentLinkedQueue<IOException>();
        path = "/discover/movies";
    }

    public List<String> getMovieIdsByDate(String start, String end) throws ParseException {
        Date startDate = new SimpleDateFormat("MM-dd-yyyy").parse(start);
        Date endDate = new SimpleDateFormat("MM-dd-yyyy").parse(end);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("release_date.gte", startDate.toString());
        params.add("release_date.lte", endDate.toString());
        params.add("api_key", apiKey);

        int min = 1;
        Optional<Integer> max = getPageCount(params, this.path);

        return combinePageResults(page -> () -> {
            JsonNode results = getResultsByPage(params, this.path, page).get("results");
            return getIdsByPage(results);
        }, min, max.orElse(1));
    }

    public Optional<Integer> getPageCount(MultiValueMap<String, String> params, String path) {
        return Optional.of(getResultsByPage(params, this.path, 1).get(
                "total_pages").asInt());
    }

    String getMovieId(String json) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(json);
        return jsonNode.get("id").asText();
    }

    List<String> combinePageResults(Function<Integer, Supplier<List<String>>> function, int min, int max) {
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

    public List<String> combinePageResultsParallel(Function<Integer, Supplier<List<String>>> function, int min, int max) {
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

    public JsonNode getResultsByPage(MultiValueMap<String, String> params, String path, Integer page) {
        MultiValueMap<String, String> newParams = new LinkedMultiValueMap<>(params);
        newParams.add("page", page.toString());
        UriComponents uriComponents = UriComponentsBuilder.fromPath(path).queryParams(newParams).build();
        String response = this.get(uriComponents.toUriString());
        JsonNode jsonNode = NullNode.getInstance();
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (IOException e) {
            exceptions.add(e);
        }
        return jsonNode;
    }

    List<String> getIdsByPage(JsonNode resultPage) {
        return resultPage.findValues("id").stream()
                .map(JsonNode::asText)
                .collect(Collectors.toList());
    }

    String get(String uri) {
        System.out.println("URI : " + uri);
        ResponseEntity<String> responseEntity = rest.getForEntity(server + uri,
                                                                  String.class);
        return responseEntity.getBody();
    }

}