package com.moviedb.explorer.jobs;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;

import org.apache.commons.io.IOUtils;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.xml.ws.Response;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class ImportMoviesTest {

    @InjectMocks
    ImportMovies testClass;

    @Mock
    RestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        String singlePageResponse = IOUtils.toString(
                this.getClass().getResourceAsStream("/single_page.json"),
                "UTF-8"
        );

        ResponseEntity<String> response = //Mockito.mock(ResponseEntity.class);
                new ResponseEntity<String>(singlePageResponse, HttpStatus.ACCEPTED);
        Mockito.when(restTemplate.getForEntity(Mockito.anyString(), Mockito.any(Class.class))).thenReturn(response);
    }

    @Test
    public void getShouldReturnNonNullResponse() throws MalformedURLException {
        assertNotNull(testClass.get("http://test.com"));
    }

    @Test
    public void shouldGetIdFromJson() throws IOException {
        String json = IOUtils.toString(
                this.getClass().getResourceAsStream("/single_result.json"),
                "UTF-8"
        );
        String actual = testClass.getMovieId(json);
        String expected = "260514";
        assertEquals(actual, expected);
    }

    @Test
    public void getMovieIdsByDate() {
    }
}