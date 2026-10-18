package edu.itc.se.lab01.task1;

class Countdown extends Thread {
    public Countdown(String name) {
        super(name);
    }

@Override
public void run() {
    try {
        for (int i=0; i>=1; i--) {
            System.out.println(getName() + ": " + i);
            Thread.sleep(200);
        }
        System.out.println(getName() + ":  lifeoff!");
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
}

/** Countdown implemented as a Runnable. */
class CountdownTask implements Runnable {  // ← removed "public"

    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        try {
            for (int i = 3; i >= 1; i--) {
                System.out.println(name + ": " + i);
                Thread.sleep(200);
            }
            System.out.println(name + ": liftoff!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}



public class Main {                 

    public static void main(String[] args) throws InterruptedException {
        boolean sequential = args.length > 0 && args[0].equals("--sequential");

        Thread apollo = new Countdown("apollo");
        Thread gemini = new Thread(new CountdownTask(), "gemini");
        Thread mercury = new Thread(() -> new CountdownTask().run(), "mercury");
        Runnable artemisTask = new CountdownTask();
        Thread artemis = Thread.ofVirtual().name("artemis").unstarted(artemisTask);

        Thread[] threads = {apollo, gemini, mercury, artemis};

        if (sequential) {
            apollo.run();
            gemini.run();
            mercury.run();
            artemisTask.run();
        } else {
            for (Thread t : threads) {
                t.start();
            }
        }
        System.out.println("Mission control: all launched");

        for (Thread t : threads) {
            t.join();
        }
        System.out.println("Mission control: all landed");

        for (Thread t : threads) {
            System.out.printf("%-8s virtual=%-5b state=%s%n",
                    t.getName(), t.isVirtual(), t.getState());
        }
    }
}