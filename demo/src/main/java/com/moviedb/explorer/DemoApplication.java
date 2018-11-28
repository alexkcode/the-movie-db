package com.moviedb.explorer;

import com.moviedb.explorer.jobs.ImportMovies;
import com.moviedb.explorer.jobs.ImportShows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootApplication
public class DemoApplication implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

    @Autowired
    ImportMovies importMovies;

    @Autowired
    ImportShows importShows;

    @Autowired
    RestTemplate restTemplate;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Running!");
        try {
            List<String> movieIds = importMovies.getMovieIdsByDate("2017-12-01", "2017-12-31");
            log.info(String.valueOf("number of movie IDs: " + movieIds.size()));
            List<String> castIds = importMovies.getAllCastIds(movieIds);
            log.info(String.valueOf("number of credit IDs: " + castIds.size()));
            List<String> showIds = importShows.getShowIdsByDate("2017-12-01", "2017-12-31");
            castIds.addAll(importMovies.getAllCastIds(showIds));

            List<String> distinctCastIds = castIds.parallelStream()
                    .distinct()
                    .collect(Collectors.toList());
            log.info("total number of cast members in movies, shows in December" + distinctCastIds.size());
        } catch (ParseException e) {
            log.error(e.getMessage());
        }
    }
}
