package com.larry.lite;

public enum TaskStatus {

    None(0), Doing(1), Error(2), Done(3), Pause(4), Cancel(5);

    int value;

    TaskStatus(int value) {
        this.value = value;
    }

    int getValue() {
        return value;
    }

    public static TaskStatus valueOf(int status) {
        if (status == 1) {
            return Doing;
        } else if (status == 2) {
            return Error;
        } else if (status == 3) {
            return Done;
        } else if (status == 4) {
            return Pause;
        } else if (status == 5) {
            return Cancel;
        } else {
            return None;
        }
    }

}
