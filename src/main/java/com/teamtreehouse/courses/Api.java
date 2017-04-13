package com.teamtreehouse.courses;

import static spark.Spark.after;
import static spark.Spark.exception;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.get;

import com.google.gson.Gson;

import com.teamtreehouse.courses.dao.CourseDao;
import com.teamtreehouse.courses.dao.ReviewDao;
import com.teamtreehouse.courses.dao.Sql2oCourseDao;
import com.teamtreehouse.courses.dao.Sql2oReviewDao;
import com.teamtreehouse.courses.exc.ApiError;
import com.teamtreehouse.courses.exc.DaoException;
import com.teamtreehouse.courses.model.Course;
import com.teamtreehouse.courses.model.Review;
import org.sql2o.Sql2o;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Api {
    public static void main(String[] args) {
        String dataSource = "jdbc:h2:~/reviews.db";
        
        if (args.length > 0) {
            if (args.length != 2) {
                System.out.println("java api expects <port> and <DataSource>");
                System.exit(0);
            }
            port(Integer.parseInt(args[0]));
            dataSource = args[1];
        
        }
        
        Sql2o sql2o = new Sql2o(
            String.format("%s;INIT=RUNSCRIPT from 'classpath:db/init.sql'", dataSource), "", "");
        CourseDao courseDao = new Sql2oCourseDao(sql2o);
        ReviewDao reviewDao = new Sql2oReviewDao(sql2o);
        Gson gson = new Gson();
        
        
        post("/courses", "application/json", (req, res) -> {
            Course course = gson.fromJson(req.body(), Course.class);
            courseDao.add(course);
            res.status(201);
            //res.type("application/json");
            return course;
        }, gson::toJson);
        
        post("/courses/:courseId/reviews", "application/json", (req, res) -> {
            int courseId = Integer.parseInt(req.params("courseId"));
            Review review = gson.fromJson(req.body(), Review.class);
            review.setCourseId(courseId);
            try {
                reviewDao.add(review);
            } catch (DaoException e) {
                throw new ApiError(500, e.getMessage());
            }
            res.status(201);
            return review;
        }, gson::toJson);
        
        get("/courses/:courseId/reviews", "application/json", (req, rest) -> {
            int courseId = Integer.parseInt(req.params("courseId"));
            return reviewDao.findByCourseId(courseId);
        }, gson::toJson);
        
        get("/courses", "application/json",
            (req, res) -> courseDao.findAll(), gson::toJson);
    
        get("/courses/:id", "application/json", (req, res) -> {
            int id = Integer.parseInt(req.params("id"));
            Course course = courseDao.findById(id);
    
            if (course == null ) {
                throw new ApiError(404, String.format("Course with id=%d not found", id));
            }
            
            return course;
        }, gson::toJson);
        
        // Jedes mal wenn ApiError geworfen wird
        // wird ein Statuscode und Nachricht gefangen
        // und gibt eine json message zurück
        exception(ApiError.class, (exc, req, res) -> {
            ApiError err = (ApiError) exc;
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("status", err.getStatus());
            jsonMap.put("errorMessage", err.getMessage());
            res.type("application/json");
            res.status(err.getStatus());
            res.body(gson.toJson(jsonMap));
        });
        
        // Jeder "response" der gesendet wird,
        // wird "application/json" haben
        after((req, res) -> res.type("application/json"));
    }

}
