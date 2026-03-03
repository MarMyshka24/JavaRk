package core.controllers;

import core.entities.Course;
import core.entities.Lesson;
import core.entities.Student;
import core.repositories.CourseRepository;
import core.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping
public class HomeController {

    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;

    @Autowired
    public HomeController(StudentRepository studentRepo,
                          CourseRepository courseRepo) {
        this.studentRepo = studentRepo;
        this.courseRepo = courseRepo;
    }

    // ===================== HOME =====================

    @GetMapping("/")
    public String home() {
        return "index";
    }

    // ===================== ASSIGN STUDENT TO COURSE =====================

    @GetMapping("/assign")
    public String assignPage(Model model) {
        model.addAttribute("students", studentRepo.findAll());
        model.addAttribute("courses", courseRepo.findAll());
        return "assign";
    }

    @PostMapping("/assign")
    public String assignStudentToCourse(@RequestParam("studentId") Long studentId,
                                        @RequestParam("courseId") Long courseId) {

        Student student = studentRepo.findById(studentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Студент не найден"));

        Course course = courseRepo.findById(courseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Курс не найден"));

        if (!student.getCourses().contains(course)) {
            student.getCourses().add(course);
            studentRepo.save(student);
        }

        return "redirect:/students";
    }

    // ===================== LESSON MANAGEMENT =====================

    @GetMapping("/courses/{courseId}/lessons/new")
    public String newLessonPage(@PathVariable("courseId") Long id,
                                Model model) {

        courseRepo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Курс не найден"));

        model.addAttribute("lesson", new Lesson());
        model.addAttribute("courseId", id);

        return "lesson-form";
    }

    @PostMapping("/courses/{courseId}/lessons")
    public String createLesson(@PathVariable("courseId") Long id,
                               @ModelAttribute Lesson lesson) {

        Course course = courseRepo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Курс не найден"));

        lesson.setCourse(course);
        course.getLessons().add(lesson);

        courseRepo.save(course);

        return "redirect:/courses";
    }
}