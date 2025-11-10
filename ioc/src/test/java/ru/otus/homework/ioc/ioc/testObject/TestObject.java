package ru.otus.homework.ioc.ioc.testObject;

public class TestObject {

    private final int x;

    private final int y;

    public TestObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int calculate() {
        return x + y;
    }
}
