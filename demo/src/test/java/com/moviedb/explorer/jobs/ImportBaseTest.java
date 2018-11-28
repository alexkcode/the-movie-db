package com.moviedb.explorer.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviedb.explorer.AppConfig;
import com.moviedb.explorer.Utility;
import com.nurkiewicz.asyncretry.RetryExecutor;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class ImportBaseTest {

    @InjectMocks
    ImportBase testClass;

    @Mock
    RestTemplate restTemplate;

    @Spy
    AppConfig config;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(testClass, "server", "http://api.themoviedb.org/3/");
        ReflectionTestUtils.setField(testClass, "objectMapper", new ObjectMapper());

        String pageOneString = IOUtils.toString(
                this.getClass().getResourceAsStream("/page1.json"),
                "UTF-8"
        );
        String pageTwoString = IOUtils.toString(
                this.getClass().getResourceAsStream("/page2.json"),
                "UTF-8"
        );

        ResponseEntity<String> responseOne = new ResponseEntity<>(pageOneString,
                                                                  HttpStatus.ACCEPTED);
        ResponseEntity<String> responseTwo = new ResponseEntity<>(pageTwoString,
                                                                  HttpStatus.ACCEPTED);
        Mockito.when(restTemplate.getForEntity(Mockito.contains("page=1"),
                                               Mockito.any(Class.class))).thenReturn(responseOne);
        Mockito.when(restTemplate.getForEntity(Mockito.contains("page=2"),
                                               Mockito.any(Class.class))).thenReturn(responseTwo);

//        Mockito.when(config.getRetryExecutor()).thenReturn(null);
    }

    @Test
    public void shouldGetPageOfResults() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        String results = testClass.getResultsByPage(params, "discover/movie", 2).toString();
        assertFalse(results.isEmpty());
        boolean containsPage = results.contains("\"page\":");
        assertTrue(containsPage);
        boolean containsResults = results.contains("\"results\":");
        assertTrue(containsResults);
        boolean containsIds = results.contains("\"id\":");
        assertTrue(containsIds);
    }

    @Test
    public void shouldGetTotalPageNumber() {
        Optional<Integer> actual = testClass.getPageCount(new LinkedMultiValueMap<>(),
                                                          "discover/movie");
        Integer expected = 2;
        assertTrue(actual.isPresent());
        assertEquals(actual.get(), expected);
    }

    @Test
    public void shouldCombineResults() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Function<Integer, Supplier<List<String>>> testSupplier = page -> () -> {
            JsonNode results = testClass.getResultsByPage(params, "discover/movie", page).get(
                    "results");
            return ImportBase.getIds(results);
        };
        List<String> actual = Utility.combinePageResults(testSupplier, config.getRetryExecutor(),
                                                         IntStream.rangeClosed(1, 2).boxed());
        JsonNode resultsOne = testClass.getResultsByPage(params, "discover/movie", 1)
                                       .get("results");
        JsonNode resultsTwo = testClass.getResultsByPage(params, "discover/movie", 2)
                                       .get("results");

        assertFalse(actual.isEmpty());
        assertEquals(actual.size(), resultsOne.size() + resultsTwo.size());
    }
}