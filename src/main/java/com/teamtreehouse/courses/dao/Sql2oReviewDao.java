package com.teamtreehouse.courses.dao;

import com.teamtreehouse.courses.exc.DaoException;
import com.teamtreehouse.courses.model.Course;
import com.teamtreehouse.courses.model.Review;

import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.util.List;

public class Sql2oReviewDao implements ReviewDao{
    private final Sql2o sql2o;
    
    public Sql2oReviewDao(Sql2o sql2o) {
        this.sql2o = sql2o;
    }
    
    @Override
    public void add(Review review) throws DaoException {
        String sql = "INSERT INTO reviews(course_id, rating, comment) VALUES (:courseId, :rating, :comment)";
        //try with resources pattern
        try(Connection con = sql2o.open()) {
            int id = (int) con.createQuery(sql)
                .bind(review)
                .executeUpdate()
                .getKey();
        
            review.setId(id);
        } catch (Sql2oException e) {
            throw new DaoException(e, "Problem adding review!");
        }
    }
    
    @Override
    public List<Review> findAll() {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery("SELECT * FROM reviews")
                .addColumnMapping("COURSE_ID", "courseId")
                .executeAndFetch(Review.class);
        }
    }
    
    @Override
    public List<Review> findByCourseId(int curseId) {
        try (Connection connection = sql2o.open()) {
            return connection.createQuery("SELECT * FROM reviews WHERE course_id = :courseId")
                .addColumnMapping("COURSE_ID", "courseId")
                .addParameter("courseId", curseId)
                .executeAndFetch(Review.class);
        }
    }
}