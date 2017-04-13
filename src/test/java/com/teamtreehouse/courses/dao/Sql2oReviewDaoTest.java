package com.teamtreehouse.courses.dao;

import static org.junit.Assert.*;

import com.teamtreehouse.courses.exc.DaoException;
import com.teamtreehouse.courses.model.Course;
import com.teamtreehouse.courses.model.Review;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.List;


public class Sql2oReviewDaoTest {
    private Connection connection;
    private Sql2oCourseDao courseDao;
    private Sql2oReviewDao reviewDao;
    private Course course;
    
    
    @Before
    public void setUp() throws Exception {
        String connectionString = "jdbc:h2:mem:testing;INIT=RUNSCRIPT from 'classpath:db/init.sql'";
        Sql2o sql2o = new Sql2o(connectionString, "", "");
        connection = sql2o.open();
        
        courseDao = new Sql2oCourseDao(sql2o);
        reviewDao = new Sql2oReviewDao(sql2o);
        
        course = new Course("TestCourse", "http://test.com");
        courseDao.add(course);
    }
    
    @After
    public void tearDown() throws Exception {
        connection.close();
    }
    
    @Test
    public void addingReviewSetsId() throws Exception {
        Review review = new Review(course.getId(), 5, "My Comment");
        int originalReviewId = review.getId();
    
        reviewDao.add(review);
    
        assertNotEquals(originalReviewId, review.getId());
    }
    
    @Test
    public void multipleReviewsAreFoundWhenTheExistsForACourse() throws Exception {
        reviewDao.add(new Review(course.getId(), 6, "Review 2"));
        reviewDao.add(new Review(course.getId(), 6, "Review 3"));
    
        List<Review> reviewList = reviewDao.findByCourseId(course.getId());
        
        assertEquals("Count not as expected", 2, reviewList.size());
    }
    
    @Test(expected = DaoException.class)
    public void addingAReviewToANotExistingCourseFails() throws Exception {
        Review review = new Review(4, 10, "New Comment");
        reviewDao.add(review);
    }
    
    @Test
    public void mustFoundAllReviews() throws Exception {
        Review review = new Review(course.getId(), 3, "New Comment 1");
        Review review2 = new Review(course.getId(), 10, "New Comment 2");
        Review review3 = new Review(course.getId(), 10, "New Comment 3");
        reviewDao.add(review);
        reviewDao.add(review2);
        reviewDao.add(review3);
        
        assertEquals(3, reviewDao.findAll().size());
    }
}