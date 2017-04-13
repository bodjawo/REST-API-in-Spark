package com.teamtreehouse.courses;

import static org.junit.Assert.*;

import com.google.gson.Gson;

import com.teamtreehouse.courses.dao.CourseDao;
import com.teamtreehouse.courses.dao.Sql2oCourseDao;
import com.teamtreehouse.courses.dao.Sql2oReviewDao;
import com.teamtreehouse.courses.model.Course;
import com.teamtreehouse.courses.model.Review;
import com.teamtreehouse.testing.ApiClient;
import com.teamtreehouse.testing.ApiResponse;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.HashMap;
import java.util.Map;
import spark.Spark;

/**
 * Created by gwo on 12.04.2017.
 */
public class ApiTest {
    
    public static final String PORT = "4568";
    public static final String TEST_DATA_SOURCE = "jdbc:h2:mem:testing";
    private Connection connection;
    private ApiClient client;
    private Gson gson;
    private Sql2oCourseDao courseDao;
    private Sql2oReviewDao reviewDao;
    
    
    @BeforeClass
    public static void startServer() {
        String[] args = {PORT, TEST_DATA_SOURCE};
        Api.main(args);
    }
    
    @AfterClass
    public static void stopServer() {
        Spark.stop();
    }
    
    
    @Before
    public void setUp() throws Exception {
        Sql2o sql2o = new Sql2o(TEST_DATA_SOURCE + ";INIT=RUNSCRIPT from 'classpath:db/init.sql'", "", "");
        courseDao = new Sql2oCourseDao(sql2o);
        reviewDao = new Sql2oReviewDao(sql2o);
        
        connection = sql2o.open();
        client = new ApiClient("http://localhost:" + PORT);
        gson = new Gson();
        
    }
    
    @After
    public void tearDown() throws Exception {
        connection.close();
    }
    
    @Test
    public void addingCoursesReturnsCreatedStatus() throws Exception {
        Map<String, String> values = new HashMap<>();
        values.put("name", "Test");
        values.put("url", "http://test.com");
    
        ApiResponse res = client.request("POST", "/courses", gson.toJson(values));
        
        assertEquals(201, res.getStatus());
    }
    
    @Test
    public void addingNewReviewMustReturnProperStatusCode() throws Exception {
        Course course = newTestCourse();
        courseDao.add(course);
        
        Map<String, Object> values = new HashMap<>();
        values.put("rating", 5);
        values.put("comment", "My comment");
        
        ApiResponse res = client.request("POST", String.format("/courses/%d/reviews", course.getId()),
            gson.toJson(values));
        
        assertEquals(201, res.getStatus());
    }
    
    @Test
    public void addingReviewToUnknownCourseThrowsError() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("rating", 5);
        values.put("comment", "My comment");
    
        ApiResponse res = client.request("POST", "/courses/24/reviews", gson.toJson(values));
    
        assertEquals(500, res.getStatus());
    }
    
    @Test
    public void coursesCanBeAccessedById() throws Exception {
        Course course = newTestCourse();
        courseDao.add(course);
        
        ApiResponse res = client.request("GET",
            "/courses/" + course.getId());
        
        Course retrieved = gson.fromJson(res.getBody(), Course.class);
        
        assertEquals(course, retrieved);
    }
    
    @Test
    public void missingCoursesReturnNotFoundStatus() throws Exception {
        ApiResponse res = client.request("GET", "/courses/42");
        
        assertEquals(404, res.getStatus());
    }
    
    @Test
    public void multipleReviewsReturnedForCourse() throws Exception {
        Course course = newTestCourse();
        courseDao.add(course);
        
        reviewDao.add(new Review(course.getId(), 3, "Comm1"));
        reviewDao.add(new Review(course.getId(), 2, "comm2"));
        reviewDao.add(new Review(course.getId(), 5, "comm3"));
        
        ApiResponse res = client.request("GET",
            String.format("/courses/%d/reviews", course.getId()));
        
        Review[] reviews = gson.fromJson(res.getBody(), Review[].class);
        
        assertEquals(3, reviews.length);
    }
    
    private Course newTestCourse() {
        return new Course("Test", "http://test.com");
    }
}