package com.irondigitalmedia.keep.Model;

public class Option {

    public String mainHeading;
    public String subHeading;

    public Option() {
    }

    public Option(String mainHeading, String subHeading) {
        this.mainHeading = mainHeading;
        this.subHeading = subHeading;
    }

    public String getMainHeading() {
        return mainHeading;
    }

    public void setMainHeading(String mainHeading) {
        this.mainHeading = mainHeading;
    }

    public String getSubHeading() {
        return subHeading;
    }

    public void setSubHeading(String subHeading) {
        this.subHeading = subHeading;
    }
}
