package com.moviedb.explorer.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.moviedb.explorer.AppConfig;
import com.moviedb.explorer.DemoApplication;
import com.moviedb.explorer.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

@Component
public abstract class ImportBase {

    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);
    private final String datePattern = "yyyy-MM-dd";

    @Value("${api.key}")
    private String apiKey;

    @Value("${credits.path}")
    private String creditsPath;

    @Value("${url}")
    private String server;

    private ConcurrentLinkedQueue<IOException> exceptions;

    public AppConfig getConfig() {
        return config;
    }

    @Autowired
    private AppConfig config;

    @Autowired
    private RestTemplate rest;

    @Autowired
    private ObjectMapper objectMapper;

    public String getApiKey() {
        return apiKey;
    }

    public String getServer() {
        return server;
    }

    public ConcurrentLinkedQueue<IOException> getExceptions() {
        return exceptions;
    }

    public RestTemplate getRest() {
        return rest;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public Optional<Integer> getPageCount(MultiValueMap<String, String> params, String path) {
        return Optional.of(getResultsByPage(params, path, 1).get(
                "total_pages").asInt());
    }

    public JsonNode getResultsByPage(MultiValueMap<String, String> params, String path,
                                     Integer page) {
        MultiValueMap<String, String> newParams = new LinkedMultiValueMap<>(params);
        newParams.add("api_key", apiKey);
        newParams.add("page", page.toString());

        UriComponents uriComponents = UriComponentsBuilder
                .fromHttpUrl(server)
                .pathSegment(path)
                .queryParams(newParams)
                .build();

        String response = rest.getForEntity(uriComponents.toUriString(),
                                            String.class).getBody();
        log.info(response);
        JsonNode jsonNode = NullNode.getInstance();
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (IOException e) {
            exceptions.add(e);
        }
        return jsonNode;
    }

    public static List<String> getIds(JsonNode resultPage) {
        return resultPage.findValuesAsText("id");
    }

    public List<String> getIdsByDate(String start, String end, String path) throws ParseException {
        Date startDate = new SimpleDateFormat(datePattern).parse(start);
        Date endDate = new SimpleDateFormat(datePattern).parse(end);

        DateFormat dateFormat = new SimpleDateFormat(datePattern);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("release_date.gte", dateFormat.format(startDate));
        params.add("release_date.lte", dateFormat.format(endDate));

        int min = 1;
        Optional<Integer> max = getPageCount(params, path);

        return Utility.combinePageResults(page -> () -> {
            JsonNode results = getResultsByPage(params, path, page).get("results");
            return ImportBase.getIds(results);
        }, config.getRetryExecutor(), IntStream.rangeClosed(min, max.orElse(1)).boxed());
    }

    String getId(String json) throws IOException {
        return getObjectMapper().readTree(json).get("id").asText();
    }

    public JsonNode getCreditsByIdAndPath(String id, String path) {
        MultiValueMap<String, String> newParams = new LinkedMultiValueMap<>();
        newParams.add("api_key", getApiKey());
        UriComponents uriComponents = UriComponentsBuilder
                .fromHttpUrl(getServer())
                .pathSegment(path, id, creditsPath)
                .queryParams(newParams)
                .build();
        ResponseEntity<String> responseEntity = getRest().getForEntity(uriComponents.toUriString(),
                                                                       String.class);
        log.info("X-RateLimit-Limit" + responseEntity.getHeaders()
                .get("X-RateLimit-Limit")
                .toString());
        log.info("X-RateLimit-Remaining" + responseEntity.getHeaders()
                .get("X-RateLimit-Remaining")
                .toString());
        String response = responseEntity.getBody();
        try {
            JsonNode jsonNode = getObjectMapper().readTree(response);
            return jsonNode.get("cast");
        } catch (IOException e) {
            getExceptions().add(e);
        }
        return NullNode.getInstance();
    };

    public abstract JsonNode getCredits(String id);

    public List<String> getAllCastIds(List<String> ids) {
        return Utility.combinePageResults(id -> () -> getCredits(id).findValuesAsText("cast_id"),
                                          getConfig().getLongRetryExecutor(),
                                          ids.stream());
    }
}

