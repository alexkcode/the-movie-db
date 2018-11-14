package com.moviedb.explorer.jobs;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static org.junit.Assert.*;

public class ImportMoviesTest {

    @Mock
    ImportMovies testClass;

    @Before
    public void setUp() throws Exception {
        testClass = new ImportMovies();

        String singlePageResponse = IOUtils.toString(
                this.getClass().getResourceAsStream("/single_page.json"),
                "UTF-8"
        );
    }

    @Test
    public void getShouldReturnNonNullResponse() {

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