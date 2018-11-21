package com.moviedb.explorer.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class ImportMovies extends ImportBase {

    @Value("${movies.path}")
    private String path;

    private HttpHeaders headers;

    public ImportMovies() {
        super();
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }

    public List<String> getMovieIdsByDate(String start, String end) throws ParseException {
        return getIdsByDate(start, end);
    }

    String getMovieId(String json) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(json);
        return jsonNode.get("id").asText();
    }
}