package com.moviedb.explorer.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = ImportMovies.class)
//@TestPropertySource("classpath:application.properties")
public class ImportMoviesTest {

    @InjectMocks
    ImportMovies testClass;

    @Mock
    RestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        ReflectionTestUtils.setField(testClass, "moviesPath", "movies");
        ReflectionTestUtils.setField(testClass, "creditsPath", "credits");

        String pageOneString = IOUtils.toString(
                this.getClass().getResourceAsStream("/movie_credits.json"),
                "UTF-8"
        );

        ResponseEntity<String> responseCredits = new ResponseEntity<>(pageOneString,
                                                                      HttpStatus.ACCEPTED);
        Mockito.when(restTemplate.getForEntity(Mockito.contains("/movies/363992/credits"),
                                               Mockito.any(Class.class)))
                .thenReturn(responseCredits);
    }

    @Test
    public void shouldGetListOfCredits() {
        JsonNode actual = testClass.getCredits("363992", new LinkedMultiValueMap<String, String>());

        assertNotNull(actual);
        assertTrue(actual.elements().next().has("cast_id"));
    }
}
