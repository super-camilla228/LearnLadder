package ru.elenaegevnateam.learnladder;
public class Result {
    public String name;
    public String clas;
    public String right_answers;
    public String percents;

    public Result() {
    }

    public Result(String name, String clas, String right_answers, String percents) {
        this.name = name;
        this.clas = clas;
        this.right_answers = right_answers;
        this.percents = percents;
    }
}

