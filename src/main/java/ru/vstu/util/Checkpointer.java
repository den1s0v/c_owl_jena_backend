package ru.vstu.util;

public class Checkpointer {
    //        'Measures time between hits. Requires the `from timeit import default_timer as timer`'
    float first, last;

    public Checkpointer() {
        reset_now();
    }

    public float timer() {
        return (float) (System.nanoTime() / 1000000) / 1000;
    }

    public void reset_now() {
        this.first = timer();
        this.last = this.first;
    }

    public float hit(String label) {

        float now = timer();
        float delta = now - this.last;
        if (label != null)
            System.out.println((!label.isEmpty() ? label : "Checkpoint") + ": " + String.format("%.3f", delta) + "s");
        this.last = now;
        return delta;
    }

    public float since_start(String label, boolean hit) {
        float now = timer();
        float delta = now - this.first;
        if (label != null)
            System.out.println((!label.isEmpty() ? label : "Total") + ": " + String.format("%.3f", delta) + "s");
        if (hit)
            this.last = now;
        return delta;
}

}
