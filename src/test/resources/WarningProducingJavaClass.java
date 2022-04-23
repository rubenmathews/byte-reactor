package com.rubenmathews.bytereactor;

import java.util.Date;

public class SimpleJavaClass implements TestRunnable {

    @Override
    public String run() {
        int something;
        Date d = new Date(86, 04, 05);
        test();
        return "warn";
    }

    @Warn
    public void test(){

    }

}
