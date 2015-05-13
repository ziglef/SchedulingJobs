package models;

import java.util.ArrayList;

public class Job {

    /*
     * id -> id of the job
     * allocated -> if its already fully allocated
     * earliestStartingTime -> the jobs earliest starting time
     * latestStartingTime -> the jobs latest starting time
     * machineOrder -> the order in which the job must be processed
     * processingTimes -> the time the job takes in each machine
     */
    private int id;
    // not in use atm area //
    private boolean allocated;
    // end not in use atm area //

    private ArrayList<Task> tasks;
    private ArrayList<Integer> machineOrder;
    private ArrayList<Integer> processingTimes;

    public Job(int id, ArrayList<Integer> machineOrder, ArrayList<Integer> processingTimes){
        this.id = id;
        this.allocated = false;

        this.tasks = new ArrayList<>();
        for (int i = 0; i < machineOrder.size(); i++) {
            this.tasks.add(new Task(id, machineOrder.get(i), processingTimes.get(i)));
        }

        this.machineOrder = machineOrder;
        this.processingTimes = processingTimes;
    }

    public int getId() {
        return id;
    }

    public ArrayList<Task> getTasks(){
        return this.tasks;
    }

    public Task getTask( int i ){
        return this.tasks.get(i);
    }

    public Task getTaskOnMachine( int m ){
        for (int i = 0; i < tasks.size(); i++) {
            if( tasks.get(i).getMachine() == m )
                return tasks.get(i);
        }

        return null;
    }

    public ArrayList<Integer> getMachineOrder() {
        return machineOrder;
    }

    public Integer getMachineOrder(int i){
        return machineOrder.get(i);
    }

    public ArrayList<Integer> getProcessingTimes() {
        return processingTimes;
    }

    public Integer getProcessingTimes(int i){
        return processingTimes.get(i);
    }

    public Integer getMachineProcessingTime(int m){
        for (int i = 0; i < machineOrder.size(); i++) {
            if( machineOrder.get(i) == m )
                return this.getProcessingTimes( i );
        }

        return -1;
    }

    public boolean isAllocated() {
        return allocated;
    }

    public void setAllocated(boolean allocated) {
        this.allocated = allocated;
    }

    @Override
    public String toString(){
        String s = "";

        for (int i = 0; i < machineOrder.size(); i++) {
            s += machineOrder.get(i) + " " + processingTimes.get(i) + " ";
        }

        return s;
    }

}
