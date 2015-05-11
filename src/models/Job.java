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
    private boolean allocated;
    private int earliestStartingTime;
    private int latestStartingTime;
    private ArrayList<Integer> machineOrder;
    private ArrayList<Integer> processingTimes;

    // TODO: implement earliest and latest starting time
    // maybe calculate them using CPM/PERT and pass them as constructor parameters
    public Job(int id, ArrayList<Integer> machineOrder, ArrayList<Integer> processingTimes){
        this.id = id;
        this. allocated = false;
        this.earliestStartingTime = 0;
        this.latestStartingTime = 0;
        this.machineOrder = machineOrder;
        this.processingTimes = processingTimes;
    }

    public int getId() {
        return id;
    }

    public int getEarliestStartingTime() {
        return earliestStartingTime;
    }

    public int getLatestStartingTime() {
        return latestStartingTime;
    }

    public ArrayList<Integer> getMachineOrder() {
        return machineOrder;
    }

    public Integer getMachineOrder(int i){
        return machineOrder.get(i);
    }

    public ArrayList<Integer> getProcessingTimes() {
        return machineOrder;
    }

    public Integer getProcessingTimes(int i){
        return machineOrder.get(i);
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
