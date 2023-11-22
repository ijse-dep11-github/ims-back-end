package lk.ijse.dep11.app.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherTO implements Serializable {
    @Null (message = "Id should be empty!")
    private Integer teacherId;
    @NotNull (message = "Name can't be empty!")
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Invalid name!")
    private String teacherName;
    @NotNull (message = "contact can't be empty!")
    @Pattern(regexp = "\\d{3}-\\d{7}", message = "Invalid format!")
    private String teacherContact;
}
