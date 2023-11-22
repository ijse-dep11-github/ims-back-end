package lk.ijse.dep11.app.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lk.ijse.dep11.app.to.CourseTo;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PreDestroy;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/courses")

public class CourseHttpController {
    private final HikariDataSource pool ;
    public CourseHttpController(){
        HikariConfig config = new HikariConfig();
        config.setUsername("root");
        config.setPassword("sql");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/dep11_ims");
        config.addDataSourceProperty("maximumPoolSize", 10);
        pool = new HikariDataSource(config);
    }
    @PreDestroy
    public void detroy(){
        pool.close();
    }
    @PostMapping(produces = "application/json" , consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseTo createCourse(@RequestBody @Validated CourseTo course){
        try (Connection connection = pool.getConnection()){
            PreparedStatement stm = connection.prepareStatement("INSERT INTO course (name,duration_in_months) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, course.getName());
            stm.setInt(2,course.getDurationInMonths());
            stm.executeUpdate();
            ResultSet generatedKeys = stm.getGeneratedKeys();
            generatedKeys.next();
            int id = generatedKeys.getInt(1);
            course.setId(id);
            return course;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(value = "/{courseId}" , consumes = "application/json")
    public void updateCourse(@PathVariable int courseId,
                             @RequestBody @Validated CourseTo course){
        try (Connection connection = pool.getConnection()) {
            PreparedStatement stmExist = connection.prepareStatement("SELECT * FROM course WHERE id = ?");
            stmExist.setInt(1, courseId);
            if (!stmExist.executeQuery().next()){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course Not Found");
            }

            PreparedStatement stm = connection
                    .prepareStatement("UPDATE course SET name = ?, duration_in_months=? WHERE id=?");
            stm.setString(1, course.getName());
            stm.setInt(2, course.getDurationInMonths());
            stm.setInt(3, courseId);
            stm.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException(e) ;
        }

    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{courseId}")
    public void deleteCourse(@PathVariable int courseId){
        try(Connection connection = pool.getConnection()){
            PreparedStatement stmExist = connection
                    .prepareStatement("SELECT * FROM course WHERE id = ?");
            stmExist.setInt(1, courseId);
            if (!stmExist.executeQuery().next()){
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course Not Found");
            }

            PreparedStatement stm = connection.prepareStatement("DELETE FROM course WHERE id=?");
            stm.setInt(1, courseId);
            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "/{courseId}" , produces = "application/json")
    public CourseTo getCourse(@PathVariable int courseId){
        try(Connection connection = pool.getConnection()){
            PreparedStatement stmExist = connection.prepareStatement("SELECT * FROM course WHERE id = ?");
            stmExist.setInt(1, courseId);
            ResultSet rst = stmExist.executeQuery();
            if (!rst.next()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course Not Found");
            }
            int id = rst.getInt("id") ;
            String name = rst.getString("name") ;
            int durationInMonths = rst.getInt("duration_in_months") ;
            return new CourseTo(id, name , durationInMonths) ;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(produces = "application/json")
    public List<CourseTo> getAllCourses(){
        try(Connection connection = pool.getConnection()){
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM course ORDER BY id");
            List<CourseTo> taskList = new LinkedList<>();
            while (rst.next()){
                int id = rst.getInt("id");
                String name = rst.getString("name");
                int durationInMonths = rst.getInt("duration_in_months");
                taskList.add(new CourseTo(id, name, durationInMonths));
            }
            return taskList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}