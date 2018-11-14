package com.moviedb.explorer.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

public class ImportMovies {

    ObjectMapper objectMapper;
    @Value("${url}")
    public String server;

    public RestTemplate getRest() {
        return rest;
    }

    public void setRest(RestTemplate rest) {
        this.rest = rest;
    }

    public RestTemplate rest;
    public HttpHeaders headers;
    @Value("${api_key}")
    public String apiKey;

    public ImportMovies() {
        objectMapper = new ObjectMapper();
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }

    public List<String> getMovieIdsByDate(String start, String end) throws ParseException {
        Date startDate = new SimpleDateFormat("MM-dd-yyyy").parse(start);
        Date endDate = new SimpleDateFormat("MM-dd-yyyy").parse(end);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("release_date.gte", startDate.toString());
        params.add("release_date.lte", endDate.toString());
        params.add("api_key", apiKey);

        Integer min = 1;
        Integer max = 1;

        List<CompletableFuture<String>> futures =
                IntStream.rangeClosed(min, max)
                        .boxed()
                        .map(page -> CompletableFuture.supplyAsync(
                             () -> getResultsByPage(params, "/discover/movies", page)))
                        .collect(Collectors.toList());

        return null;
    }

    public Integer getPageCount(MultiValueMap<String, String> params, String path) {

        return null;
    }

    public String getMovieId(String json) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(json);
        String movieId = jsonNode.get("id").asText();
        return movieId;
    }

    public String processAllPages(MultiValueMap<String, String> params, String path) {

        Integer min = 1;
        Integer max = 1;

        List<CompletableFuture<String>> futures =
                IntStream.rangeClosed(min, max)
                        .boxed()
                        .map(page -> CompletableFuture.supplyAsync(
                                () -> getResultsByPage(params, path, page)))
                        .collect(Collectors.toList());
        return null;
    }

    public String getResultsByPage(MultiValueMap<String, String> params, String path, Integer page) {
        params.add("page", page.toString());
        UriComponents uriComponents = UriComponentsBuilder.fromPath(path).queryParams(params).build();
        String response = this.get(uriComponents.toUriString());
        String results = "";
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            results = jsonNode.get("results").asText();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return results;
    }

    public String get(String uri) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri,
                                                              HttpMethod.GET,
                                                              requestEntity,
                                                              String.class);
        return responseEntity.getBody();
    }

}