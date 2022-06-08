package ru.vstu.util;

// Luke Hutchison's examples for his Producer class.
// https://stackoverflow.com/a/58589360/12824563


public class TryProducer {

    static void testMethod() throws InterruptedException {
        for (Integer item : new Producer<Integer>(/* queueSize = */ 1) {
            @Override
            public void producer() {
                for (int i = 0; i < 20; i++) {
                    System.out.println("Producing " + i);
                    produce(i);
                }
                System.out.println("Producer exiting");
            }
        }) {
            System.out.println("  Consuming " + item);
            Thread.sleep(300);

            /// test
            if (item > 10) {
                System.out.println("  Stopping consuming ... ");
                break;
            }
        }
    }

        static void textLambda() throws InterruptedException {

        for (Integer item : new Producer<Integer>(/* queueSize = */ 1, producer -> {
            for (int i = 0; i < 20; i++) {
                System.out.println("Producing " + i);
                producer.produce(i);
            }
            System.out.println("Producer exiting");
        })) {
            System.out.println("  Consuming " + item);
            Thread.sleep(400);
        }
    }



    public static void main(String[] args) throws InterruptedException {
//        textLambda();
        testMethod();
    }
}
