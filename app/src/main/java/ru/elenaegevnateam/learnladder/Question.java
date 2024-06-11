package ru.elenaegevnateam.learnladder;

public class Question {
    public String ans1;
    public String ans2;
    public String ans3;
    public String ans4;
    public String answer;
    public String q_word;
    public String isDeleted;

    public Question() {
    }

    public Question(String ans1, String ans2, String ans3, String ans4, String answer, String q_word, String isDeleted) {
        this.ans1 = ans1;
        this.ans2 = ans2;
        this.ans3 = ans3;
        this.ans4 = ans4;
        this.answer = answer;
        this.q_word = q_word;
        this.isDeleted = isDeleted;
    }
}
