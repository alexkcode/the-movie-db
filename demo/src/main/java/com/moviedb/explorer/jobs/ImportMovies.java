package com.moviedb.explorer.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeCreator;
import com.fasterxml.jackson.databind.node.NullNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

@Service
//@PropertySource("classpath:/application.properties")
public class ImportMovies extends ImportBase {

    @Value("${movies.discover.path}")
    private String discoverPath;

    @Value("${movies.path}")
    private String moviesPath;

    @Value("${credits.path}")
    private String creditsPath;

    private HttpHeaders headers;

    public ImportMovies() {
        super();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }

    public List<String> getMovieIdsByDate(String start, String end) throws ParseException {
        return getIdsByDate(start, end, this.discoverPath);
    }

    String getMovieId(String json) throws IOException {
        return objectMapper.readTree(json).get("id").asText();
    }

    public JsonNode getCredits(String id, MultiValueMap<String, String> params) {
        MultiValueMap<String, String> newParams = new LinkedMultiValueMap<>(params);
        newParams.add("api_key", getApiKey());
        UriComponents uriComponents = UriComponentsBuilder
                .fromPath(getServer())
                .pathSegment(moviesPath, id, creditsPath)
                .queryParams(newParams)
                .build();
        String response = getRest().getForEntity(uriComponents.toUriString(),
                                                 String.class).getBody();
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("cast");
        } catch (IOException e) {
            getExceptions().add(e);
        }
//        return Collections.emptyList();
        return NullNode.getInstance();
    }
}