package ru.elenaegevnateam.learnladder;

public class Student implements Comparable<Student>{
    public String name;
    public String id;
    public int coins;

    public Student() {
    }

    public Student(String name, String id, int coins) {
        this.name = name;
        this.id = id;
        this.coins = coins;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public int getCoins() {
        return coins;
    }

    public int compareTo(Student otherStudent) {
        return Integer.compare(otherStudent.getCoins(), this.coins);
    }
    public void setCoins(int newCoins) {
        this.coins = newCoins;
    }
    public void setName(String newName) {
        this.name = newName;
    }
}
