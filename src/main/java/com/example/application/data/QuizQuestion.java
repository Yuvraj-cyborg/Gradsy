package com.example.application.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_questions")
public class QuizQuestion extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "quiz_id")
    @NotNull
    private Quiz quiz;

    @Column(name = "question_text")
    @NotEmpty
    private String questionText;

    @Column(name = "question_type")
    @NotEmpty
    private String questionType;

    @Column(name = "correct_answer")
    @NotEmpty
    private String correctAnswer;

    private Integer points = 1;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizAnswer> answers = new ArrayList<>();

    // Getters and Setters
    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public List<QuizAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuizAnswer> answers) {
        this.answers = answers;
    }

    public void addAnswer(QuizAnswer answer) {
        answers.add(answer);
        answer.setQuestion(this);
    }

    public void removeAnswer(QuizAnswer answer) {
        answers.remove(answer);
        answer.setQuestion(null);
    }
}