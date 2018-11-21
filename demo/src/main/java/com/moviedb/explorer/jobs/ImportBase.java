package com.moviedb.explorer.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.moviedb.explorer.Utility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class ImportBase {
    private String path;
    ObjectMapper objectMapper;
    @Value("${api.key}")
    private String apiKey;
    private ConcurrentLinkedQueue<IOException> exceptions;
    @Value("${url}")
    private String server;
    @Autowired
    private RestTemplate rest;

    public ImportBase() {
        objectMapper = new ObjectMapper();
        exceptions = new ConcurrentLinkedQueue<IOException>();
    }

    public ConcurrentLinkedQueue<IOException> getExceptions() {
        return exceptions;
    }

    public Optional<Integer> getPageCount(MultiValueMap<String, String> params, String path) {
        return Optional.of(getResultsByPage(params, path, 1).get(
                "total_pages").asInt());
    }

    public JsonNode getResultsByPage(MultiValueMap<String, String> params, String path,
                                     Integer page) {
        MultiValueMap<String, String> newParams = new LinkedMultiValueMap<>(params);
        newParams.add("page", page.toString());
        UriComponents uriComponents = UriComponentsBuilder.fromPath(path)
                .queryParams(newParams)
                .build();
        String response = rest.getForEntity(server + uriComponents,
                                            String.class).getBody();
        JsonNode jsonNode = NullNode.getInstance();
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (IOException e) {
            exceptions.add(e);
        }
        return jsonNode;
    }

    public static List<String> getIdsByPage(JsonNode resultPage) {
        return resultPage.findValues("id").stream()
                .map(JsonNode::asText)
                .collect(Collectors.toList());
    }

    public List<String> getIdsByDate(String start, String end) throws ParseException {
        Date startDate = new SimpleDateFormat("MM-dd-yyyy").parse(start);
        Date endDate = new SimpleDateFormat("MM-dd-yyyy").parse(end);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("release_date.gte", startDate.toString());
        params.add("release_date.lte", endDate.toString());
        params.add("api_key", apiKey);

        int min = 1;
        Optional<Integer> max = getPageCount(params, this.path);

        return Utility.combinePageResults(page -> () -> {
            JsonNode results = getResultsByPage(params, this.path, page).get("results");
            return ImportBase.getIdsByPage(results);
        }, min, max.orElse(1));
    }
}

