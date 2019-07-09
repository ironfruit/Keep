package com.irondigitalmedia.keep.Model;

public class Instruction {

    public String uid;
    public String num;
    public String step;

    public Instruction() {
    }

    public Instruction(String uid, String num, String step) {
        this.uid = uid;
        this.num = num;
        this.step = step;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }
}
