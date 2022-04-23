package com.rubenmathews.bytereactor;

public class InnerClassWithMultipleClass implements TestRunnable {
    @Override
    public String run() {
        return new InnerClass().test() + new RunnableCode().run();
    }

    class InnerClass {
        public String test() {
            return "Response From Inner Class.";
        }
    }
}

class RunnableCode {

    public String run() {
        return "Result From Another class";
    }
}


