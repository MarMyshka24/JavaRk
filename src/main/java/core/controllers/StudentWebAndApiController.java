package core.controllers;

import core.entities.Student;
import core.entities.StudentProfile;
import core.repositories.CourseRepository;
import core.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping
public class StudentWebAndApiController {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Autowired
    public StudentWebAndApiController(StudentRepository studentRepository,
                                      CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    // ===================== WEB =====================

    @GetMapping("/students")
    public String listStudents(Model model) {
        List<Student> students = studentRepository.findAll();
        model.addAttribute("students", students);
        return "students";
    }

    @GetMapping("/students/new")
    public String newStudentPage(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("profile", new StudentProfile());
        return "student-form";
    }

    @PostMapping("/students")
    public String saveStudent(@ModelAttribute Student student,
                              @RequestParam String address,
                              @RequestParam String phone,
                              @RequestParam String birthDate,
                              Model model) {

        if (studentRepository.findByEmail(student.getEmail()).isPresent()) {
            model.addAttribute("error", "Email уже используется другим студентом");
            model.addAttribute("student", student);
            model.addAttribute("profile", new StudentProfile());
            return "student-form";
        }

        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setAddress(address);
        studentProfile.setPhone(phone);
        studentProfile.setBirthDate(LocalDate.parse(birthDate));

        student.setProfile(studentProfile);

        studentRepository.save(student);

        return "redirect:/students";
    }

    @DeleteMapping("/students/{id}")
    public String deleteStudentWeb(@PathVariable("id") Long studentId) {
        studentRepository.deleteById(studentId);
        return "redirect:/students";
    }

    // ===================== API =====================

    @RestController
    @RequestMapping("/api/students")
    static class StudentApi {

        private final StudentRepository studentRepo;
        private final CourseRepository courseRepo;

        @Autowired
        public StudentApi(StudentRepository studentRepo,
                          CourseRepository courseRepo) {
            this.studentRepo = studentRepo;
            this.courseRepo = courseRepo;
        }

        @GetMapping
        public List<Student> fetchAll() {
            return studentRepo.findAll();
        }

        @GetMapping("/{id}")
        public ResponseEntity<Student> fetchById(@PathVariable Long id) {
            return studentRepo.findById(id)
                    .map(student -> ResponseEntity.ok(student))
                    .orElse(ResponseEntity.notFound().build());
        }

        @PostMapping
        public Student create(@RequestBody Student student) {
            return studentRepo.save(student);
        }

        @PostMapping("/{studentId}/courses/{courseId}")
        public ResponseEntity<String> assignCourse(
                @PathVariable("studentId") Long studentId,
                @PathVariable("courseId") Long courseId) {

            Student student = studentRepo.findById(studentId).orElse(null);
            if (student == null) {
                return ResponseEntity.notFound().build();
            }

            courseRepo.findById(courseId).ifPresent(course -> {
                student.getCourses().add(course);
                studentRepo.save(student);
            });

            return ResponseEntity.ok("Курс назначен");
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<String> delete(@PathVariable Long id) {
            if (!studentRepo.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            studentRepo.deleteById(id);
            return ResponseEntity.ok("Студент удалён");
        }
    }
}