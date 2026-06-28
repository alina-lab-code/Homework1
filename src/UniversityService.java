


import java.util.*;
import java.util.stream.Collectors;

public class UniversityService {

    //  here we have students names in department sorted by alphabet
    public List<String> studentsInDepartment(List<Student> students, String departmentName) {
        return students.stream()
                .filter(s -> s.getCourses().stream()
                        .anyMatch(c -> c.getDepartment().getName().equals(departmentName)))
                .map(Student::getName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    //  name of the student with max credits
    public String topStudentByCredits(List<Student> students) {
        return students.stream()
                .max(Comparator.comparingInt(s -> s.getCourses().stream()
                        .mapToInt(Course::getCredits).sum()))
                .map(Student::getName)
                .orElse(null);
    }

    public Map<String, List<String>> courseTitlesByDepartmentFromYear(List<Student> students, int fromYear) {
        return students.stream()
                .flatMap(s -> s.getCourses().stream()) // stream of all students
                .filter(c -> c.getYear() >= fromYear)  // filter by year
                .collect(Collectors.groupingBy(
                        c -> c.getDepartment().getName(),
                        Collectors.mapping(Course::getTitle, Collectors.toList())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().distinct().collect(Collectors.toList())
                ));
    }
}
