package com.moviedb.explorer.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

@Component
public class ImportMovies extends ImportBase {

    private static final Logger log = LoggerFactory.getLogger(ImportBase.class);

    @Value("${movies.discover.path}")
    private String discoverPath;

    @Value("${movies.path}")
    private String moviesPath;

    public List<String> getMovieIdsByDate(String start, String end) throws ParseException {
        return getIdsByDate(start, end, this.discoverPath);
    }

    public JsonNode getCredits(String id) {
        return getCreditsByIdAndPath(id, moviesPath);
    }

}