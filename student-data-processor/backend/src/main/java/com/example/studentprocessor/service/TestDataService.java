package com.example.studentprocessor.service;

import com.example.studentprocessor.entity.Student;
import com.example.studentprocessor.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class TestDataService {

    @Autowired
    private StudentRepository studentRepository;

    public String generateTestStudents(int count) {
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

                // Save in batches of 100 to avoid memory issues
                if (students.size() == 100) {
                    studentRepository.saveAll(students);
                    students.clear();
                }
            }

            // Save remaining students
            if (!students.isEmpty()) {
                studentRepository.saveAll(students);
            }

            return "Successfully generated " + count + " test students";

        } catch (Exception e) {
            return "Error generating test data: " + e.getMessage();
        }
    }
}
