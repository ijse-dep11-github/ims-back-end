package lk.ijse.dep11.app.api;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lk.ijse.dep11.app.to.TeacherTO;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PreDestroy;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/teachers")
@CrossOrigin
public class TeacherHttpController {
    HikariDataSource pool;

    public TeacherHttpController() {
        HikariConfig config = new HikariConfig();
        config.setUsername("root");
        config.setPassword("mysql123");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://localhost:3306/dep11_ims");
        config.setMaximumPoolSize(10);
        pool = new HikariDataSource(config);
    }

    @PreDestroy
    public void destroy() {
        pool.close();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = "application/json", produces = "application/json")
    public TeacherTO createTeacher(@RequestBody @Validated TeacherTO teacher) {
        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement("INSERT INTO teacher (name, contact) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, teacher.getTeacherName());
            stm.setString(2, teacher.getTeacherContact());
            stm.executeUpdate();

            ResultSet rst = stm.getGeneratedKeys();
            rst.next();
            int id = rst.getInt(1);
            teacher.setTeacherId(id);
            return teacher;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping(value = "/{teacherId}", consumes = "application/json")
    public void updateTeacher(@PathVariable int teacherId,
                              @RequestBody @Validated TeacherTO teacher) {
        try (Connection connection = pool.getConnection()) {
            PreparedStatement stmExists = connection.prepareStatement("SELECT * FROM teacher WHERE id = ?");
            stmExists.setInt(1, teacherId);
            if (!stmExists.executeQuery().next()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found!");
            }
            PreparedStatement stm = connection.prepareStatement("UPDATE teacher SET name = ?, contact = ? WHERE id = ?");
            stm.setString(1, teacher.getTeacherName());
            stm.setString(2, teacher.getTeacherContact());
            stm.setInt(3, teacherId);
            stm.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{teacherId}")
    public void deleteTeacher(@PathVariable int teacherId) {
        try (Connection connection = pool.getConnection()) {
            PreparedStatement stmExists = connection.prepareStatement("SELECT * FROM teacher WHERE id = ?");
            stmExists.setInt(1, teacherId);
            if (!stmExists.executeQuery().next()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found!");
            }
            PreparedStatement stm = connection.prepareStatement("DELETE FROM teacher WHERE id = ?");




        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(value = "/{teacherId}", produces = "application/json")
    public TeacherTO getTeacherDetails(@PathVariable int teacherId) {
        try (Connection connection = pool.getConnection()) {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM teacher WHERE id = ?");
            stm.setInt(1, teacherId);
            ResultSet rst = stm.executeQuery();
            if (!rst.next()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found!");
            }
            String name = rst.getString("name");
            String contact = rst.getString("contact");
            return new TeacherTO(teacherId, name, contact);


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

        @GetMapping(produces = "application/json")
    public List<TeacherTO> getAllTeachers() {
        try (Connection connection = pool.getConnection()) {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM teacher ORDER BY id");
            List<TeacherTO> teacherList = new LinkedList<>();
            while (rst.next()) {
                int id = rst.getInt("id");
                String name = rst.getString("name");
                String contact = rst.getString("contact");
                teacherList.add(new TeacherTO(id, name, contact));

            }
            return teacherList;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
