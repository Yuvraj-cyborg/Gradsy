-- Sample users (password is 'password' encoded with BCrypt)
INSERT INTO users (id, username, password, role, version, email) VALUES 
(1, 'admin', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'ADMIN', 1, 'admin@example.com'),
(2, 'teacher1', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'TEACHER', 1, 'teacher1@example.com'),
(3, 'teacher2', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'TEACHER', 1, 'teacher2@example.com'),
(4, 'student1', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'STUDENT', 1, 'student1@example.com'),
(5, 'student2', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'STUDENT', 1, 'student2@example.com'),
(6, 'student3', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'STUDENT', 1, 'student3@example.com');

-- Sample teacher profiles
INSERT INTO teacher_profiles (id, user_id, first_name, last_name, subject_area, version) VALUES 
(1, 2, 'John', 'Smith', 'Mathematics', 1),
(2, 3, 'Emma', 'Johnson', 'Science', 1);

-- Sample student profiles
INSERT INTO student_profiles (id, user_id, first_name, last_name, grade_level, version) VALUES 
(1, 4, 'Michael', 'Brown', 10, 1),
(2, 5, 'Sarah', 'Davis', 11, 1),
(3, 6, 'David', 'Wilson', 9, 1);

-- Sample learning materials
INSERT INTO learning_materials (id, title, description, uploaded_by, created_at, updated_at, version) VALUES 
(1, 'Algebra Basics', 'Introduction to basic algebraic concepts', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
(2, 'Chemistry 101', 'Fundamentals of chemistry', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

-- Sample quiz
INSERT INTO quizzes (id, title, description, created_by, created_at, duration_minutes, is_active, version) VALUES 
(1, 'Algebra Quiz 1', 'Test your knowledge of basic algebra', 2, CURRENT_TIMESTAMP, 20, true, 1),
(2, 'Chemistry Quiz', 'Test your understanding of basic chemistry concepts', 3, CURRENT_TIMESTAMP, 30, true, 1);

-- Sample quiz questions
INSERT INTO quiz_questions (id, quiz_id, question_text, question_type, correct_answer, points, version) VALUES 
(1, 1, 'What is the value of x in the equation 2x + 5 = 15?', 'MULTIPLE_CHOICE', '5', 1, 1),
(2, 1, 'Solve for y: 3y - 6 = 12', 'MULTIPLE_CHOICE', '6', 1, 1),
(3, 2, 'What is the chemical symbol for water?', 'MULTIPLE_CHOICE', 'H2O', 1, 1),
(4, 2, 'What is the atomic number of Carbon?', 'MULTIPLE_CHOICE', '6', 1, 1);

-- Sample quiz answers
INSERT INTO quiz_answers (id, question_id, answer_text, is_correct, display_order, version) VALUES 
(1, 1, '3', false, 0, 1),
(2, 1, '5', true, 1, 1),
(3, 1, '7', false, 2, 1),
(4, 1, '10', false, 3, 1),
(5, 2, '4', false, 0, 1),
(6, 2, '6', true, 1, 1),
(7, 2, '8', false, 2, 1),
(8, 2, '9', false, 3, 1),
(9, 3, 'H2O', true, 0, 1),
(10, 3, 'CO2', false, 1, 1),
(11, 3, 'NaCl', false, 2, 1),
(12, 3, 'O2', false, 3, 1),
(13, 4, '5', false, 0, 1),
(14, 4, '6', true, 1, 1),
(15, 4, '7', false, 2, 1),
(16, 4, '8', false, 3, 1);
