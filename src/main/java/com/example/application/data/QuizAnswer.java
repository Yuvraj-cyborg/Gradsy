package com.example.application.data;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "quiz_answers")
public class QuizAnswer extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "question_id")
    @NotNull
    private QuizQuestion question;

    @Column(name = "answer_text")
    @NotEmpty
    private String answerText;

    @Column(name = "is_correct")
    private boolean correct = false;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    // Getters and Setters
    public QuizQuestion getQuestion() {
        return question;
    }

    public void setQuestion(QuizQuestion question) {
        this.question = question;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
} 