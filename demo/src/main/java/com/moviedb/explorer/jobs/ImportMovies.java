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

    @Value("${credits.path}")
    private String creditsPath;

    public List<String> getMovieIdsByDate(String start, String end) throws ParseException {
        return getIdsByDate(start, end, this.discoverPath);
    }

    public JsonNode getCredits(String id) {
        MultiValueMap<String, String> newParams = new LinkedMultiValueMap<>();
        newParams.add("api_key", getApiKey());
        UriComponents uriComponents = UriComponentsBuilder
                .fromHttpUrl(getServer())
                .pathSegment(moviesPath, id, creditsPath)
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
    }
//
//    public List<String> getAllCastIds(List<String> ids) {
//        return Utility.combinePageResults(id -> () -> getCredits(id).findValuesAsText("cast_id"),
//                                          getConfig().getLongRetryExecutor(),
//                                          ids.stream());
//    }

}