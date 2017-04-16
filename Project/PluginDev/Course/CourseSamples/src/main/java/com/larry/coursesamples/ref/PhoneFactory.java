package com.larry.coursesamples.ref;

/**
 * Created by Larry on 2017/4/16.
 */

public class PhoneFactory {

    public static Phone getInstance(String name) {

        if (name.equals("Nexus")) {
            return new Nexus();
        } else if (name.equals("Glexy")) {
            return new Glexy();
        }
        return new Phone();
    }

    public static Phone getReflectInstance(String className) {

        Phone phone = null;
        try {
            phone = (Phone) Class.forName(className).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phone;

    }

}
