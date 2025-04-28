-- Create role enum type
CREATE TYPE user_role AS ENUM ('STUDENT', 'INSTRUCTOR', 'ADMIN');

-- Users table for authentication and basic info
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role user_role NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Student profiles
CREATE TABLE student_profiles (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    grade_level INTEGER,
    date_of_birth DATE,
    UNIQUE(user_id)
);

-- Teacher profiles
CREATE TABLE teacher_profiles (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    subject_area VARCHAR(100),
    UNIQUE(user_id)
);

-- Learning materials/notes
CREATE TABLE learning_materials (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    file_path VARCHAR(500),
    uploaded_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Quizzes
CREATE TABLE quizzes (
    id SERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    created_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duration_minutes INTEGER DEFAULT 30,
    is_active BOOLEAN DEFAULT true
);

-- Quiz questions
CREATE TABLE quiz_questions (
    id SERIAL PRIMARY KEY,
    quiz_id INTEGER REFERENCES quizzes(id),
    question_text TEXT NOT NULL,
    question_type VARCHAR(20) NOT NULL, -- 'MULTIPLE_CHOICE', 'TRUE_FALSE', etc.
    correct_answer TEXT NOT NULL,
    points INTEGER DEFAULT 1
);

-- Quiz answers/options
CREATE TABLE quiz_answers (
    id SERIAL PRIMARY KEY,
    question_id INTEGER REFERENCES quiz_questions(id),
    answer_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT false
);

-- Student quiz attempts
CREATE TABLE quiz_attempts (
    id SERIAL PRIMARY KEY,
    quiz_id INTEGER REFERENCES quizzes(id),
    student_id INTEGER REFERENCES users(id),
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    score DECIMAL(5,2),
    completed BOOLEAN DEFAULT false
);

-- Student quiz responses
CREATE TABLE quiz_responses (
    id SERIAL PRIMARY KEY,
    attempt_id INTEGER REFERENCES quiz_attempts(id),
    question_id INTEGER REFERENCES quiz_questions(id),
    student_answer TEXT,
    is_correct BOOLEAN,
    points_earned INTEGER DEFAULT 0
);

-- Insert some sample data for testing
INSERT INTO users (username, password, email, role) VALUES
('teacher1', '$2a$10$encrypted', 'teacher1@school.com', 'INSTRUCTOR'),
('student1', '$2a$10$encrypted', 'student1@school.com', 'STUDENT'),
('admin1', '$2a$10$encrypted', 'admin1@school.com', 'ADMIN');

-- Create indexes for better performance
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_quiz_attempts_student ON quiz_attempts(student_id);
CREATE INDEX idx_quiz_questions_quiz ON quiz_questions(quiz_id);
CREATE INDEX idx_learning_materials_uploaded ON learning_materials(uploaded_by);