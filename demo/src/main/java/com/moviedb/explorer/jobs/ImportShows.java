package com.moviedb.explorer.jobs;

import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class ImportShows extends ImportBase {

    @Value("${tv.discover.path}")
    private String path;

    public List<String> getShowIdsByDate(String start, String end) throws ParseException {
        return getIdsByDate(start, end, this.path);
    }

    String getShowId(String json) throws IOException {
        return objectMapper.readTree(json).get("id").asText();
    }
}
