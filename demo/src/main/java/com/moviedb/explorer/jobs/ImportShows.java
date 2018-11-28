package com.moviedb.explorer.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.List;

@Component
public class ImportShows extends ImportBase {

    private static final Logger log = LoggerFactory.getLogger(ImportBase.class);

    @Value("${tv.discover.path}")
    private String path;

    @Value("${tv.path}")
    private String showsPath;

    @Value("${credits.path}")
    private String creditsPath;

    public List<String> getShowIdsByDate(String start, String end) throws ParseException {
        return getIdsByDate(start, end, this.path);
    }

    public JsonNode getCredits(String id) {
        return getCreditsByIdAndPath(id, showsPath);
    }

}
