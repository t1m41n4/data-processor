package com.example.studentprocessor.controller;

import com.example.studentprocessor.entity.Student;
import com.example.studentprocessor.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/test-data")
@CrossOrigin(origins = "http://localhost:4200")
public class TestDataController {

    @Autowired
    private StudentRepository studentRepository;

    @PostMapping("/generate/{count}")
    public ResponseEntity<Map<String, Object>> generateTestData(@PathVariable int count) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Clear existing data
            studentRepository.deleteAll();

            List<Student> students = new ArrayList<>();
            Random random = new Random();

            String[] firstNames = {"John", "Jane", "Michael", "Sarah", "David", "Emma", "James", "Olivia", "Robert", "Sophia",
                                 "William", "Isabella", "Christopher", "Mia", "Matthew", "Charlotte", "Anthony", "Amelia", "Mark", "Harper",
                                 "Steven", "Evelyn", "Paul", "Abigail", "Andrew", "Emily", "Joshua", "Elizabeth", "Kenneth", "Sofia",
                                 "Kevin", "Avery", "Brian", "Ella", "George", "Madison", "Timothy", "Scarlett", "Ronald", "Victoria",
                                 "Jason", "Aria", "Edward", "Grace", "Jeffrey", "Chloe", "Ryan", "Camila", "Jacob", "Penelope"};

            String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
                                "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin",
                                "Lee", "Perez", "Thompson", "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson",
                                "Walker", "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Flores",
                                "Green", "Adams", "Nelson", "Baker", "Hall", "Rivera", "Campbell", "Mitchell", "Carter", "Roberts"};

            String[] classes = {"Math A", "Math B", "Science A", "Science B", "English A", "English B", "History A", "History B",
                              "Physics A", "Physics B", "Chemistry A", "Chemistry B", "Biology A", "Biology B", "Art A", "Art B",
                              "Music A", "Music B", "Computer Science A", "Computer Science B"};

            for (int i = 1; i <= count; i++) {
                Student student = new Student();
                student.setStudentId((long) (1000 + i));

                String firstName = firstNames[random.nextInt(firstNames.length)];
                String lastName = lastNames[random.nextInt(lastNames.length)];
                student.setFirstName(firstName);
                student.setLastName(lastName);
                student.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@student.edu");
                student.setPhone("555" + String.format("%07d", random.nextInt(10000000)));
                student.setClassName(classes[random.nextInt(classes.length)]);

                // Generate realistic score distribution
                int score;
                double rand = random.nextDouble();
                if (rand < 0.1) { // 10% excellent (90-100)
                    score = 90 + random.nextInt(11);
                } else if (rand < 0.3) { // 20% good (80-89)
                    score = 80 + random.nextInt(10);
                } else if (rand < 0.7) { // 40% average (60-79)
                    score = 60 + random.nextInt(20);
                } else { // 30% below average (40-59)
                    score = 40 + random.nextInt(20);
                }
                student.setScore(score);

                // Generate random birth date (18-25 years old)
                int age = 18 + random.nextInt(8);
                LocalDate birthDate = LocalDate.now().minusYears(age).minusDays(random.nextInt(365));
                student.setDob(birthDate);

                students.add(student);
            }

            // Save all students in batch
            studentRepository.saveAll(students);

            response.put("success", true);
            response.put("message", "Successfully generated " + count + " test students");
            response.put("studentsCreated", count);
            response.put("totalStudents", studentRepository.count());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating test data: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getTestDataStatus() {
        Map<String, Object> response = new HashMap<>();
        long studentCount = studentRepository.count();

        response.put("totalStudents", studentCount);
        response.put("hasTestData", studentCount > 0);
        response.put("message", studentCount > 0 ?
            "Database contains " + studentCount + " students" :
            "Database is empty - no test data available");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearTestData() {
        Map<String, Object> response = new HashMap<>();

        try {
            long deletedCount = studentRepository.count();
            studentRepository.deleteAll();

            response.put("success", true);
            response.put("message", "Successfully cleared all test data");
            response.put("studentsDeleted", deletedCount);
            response.put("totalStudents", studentRepository.count());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error clearing test data: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(response);
        }
    }
}
