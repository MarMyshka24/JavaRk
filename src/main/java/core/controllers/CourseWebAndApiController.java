package core.controllers;

import core.entities.Course;
import core.entities.Lesson;
import core.repositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping
public class CourseWebAndApiController {

    private final CourseRepository courseRepo;

    @Autowired
    public CourseWebAndApiController(CourseRepository courseRepo) {
        this.courseRepo = courseRepo;
    }

    // ===================== WEB PART =====================

    @GetMapping("/courses")
    public String getCourses(Model model) {
        List<Course> courseList = courseRepo.findAll();
        model.addAttribute("courses", courseList);
        return "courses";
    }

    @GetMapping("/courses/new")
    public String createCoursePage(Model model) {
        model.addAttribute("course", new Course());
        return "course-form";
    }

    @PostMapping("/courses")
    public String saveCourse(@ModelAttribute Course course) {
        courseRepo.save(course);
        return "redirect:/courses";
    }

    @GetMapping("/courses/{id}/lessons/new")
    public String createLessonPage(@PathVariable("id") Long courseId,
                                   Model model) {
        model.addAttribute("lesson", new Lesson());
        model.addAttribute("courseId", courseId);
        return "lesson-form";
    }

    @PostMapping("/courses/{id}/lessons")
    public String saveLesson(@PathVariable("id") Long courseId,
                             @ModelAttribute Lesson lesson) {

        courseRepo.findById(courseId).ifPresent(course -> {
            lesson.setCourse(course);
            course.getLessons().add(lesson);
            courseRepo.save(course);
        });

        return "redirect:/courses";
    }

    // ===================== API PART =====================

    @RestController
    @RequestMapping("/api/courses")
    static class CourseApi {

        private final CourseRepository courseRepository;

        @Autowired
        public CourseApi(CourseRepository courseRepository) {
            this.courseRepository = courseRepository;
        }

        @GetMapping
        public List<Course> getAllCourses() {
            return courseRepository.findAll();
        }

        @GetMapping("/{id}")
        public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
            return courseRepository.findById(id)
                    .map(course -> ResponseEntity.ok(course))
                    .orElse(ResponseEntity.notFound().build());
        }

        @PostMapping
        public Course addCourse(@RequestBody Course course) {
            return courseRepository.save(course);
        }

        @PostMapping("/{courseId}/lessons")
        public ResponseEntity<String> addLessonToCourse(
                @PathVariable Long courseId,
                @RequestBody Lesson lesson) {

            return courseRepository.findById(courseId)
                    .map(course -> {
                        lesson.setCourse(course);
                        course.getLessons().add(lesson);
                        courseRepository.save(course);
                        return ResponseEntity.ok("Урок добавлен");
                    })
                    .orElse(ResponseEntity.notFound().build());
        }
    }
}