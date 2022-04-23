package com.rubenmathews.bytereactor;

public class MultipleClass implements TestRunnable {
    @Override
    public String run() {
        return new RunnableCode().run();
    }
}

class RunnableCode {

    public String run() {
        return "Result From Another class";
    }
}


