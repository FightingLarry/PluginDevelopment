package com.larry.coursesamples.ref;

/**
 * Created by Larry on 2017/4/3.
 */

public class Phone implements IPhone {

    public String os;

    private String name;

    private int price;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public void call() {

    }

    @Override
    public String toString() {
        return "[name=" + name + ",price=" + price + "]";
    }
}
