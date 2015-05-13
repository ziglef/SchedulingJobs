package models;

public class Task {

    private int machine;
    private int job;
    private int processingTime;

    public Task( int job, int machine, int processingTime ){
        this.job = job;
        this.machine = machine;
        this.processingTime = processingTime;
    }

    public int getMachine() {
        return machine;
    }

    public int getJob() {
        return job;
    }

    public int getProcessingTime() {
        return processingTime;
    }
}
