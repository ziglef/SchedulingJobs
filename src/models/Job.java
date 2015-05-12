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
    private int earliestEndingTime;
    private int latestEndingTime;
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

    public void setEarliestStartingTime(int earliestStartingTime) {
        this.earliestStartingTime = earliestStartingTime;
    }

    public int getLatestStartingTime() {
        return latestStartingTime;
    }

    public void setLatestStartingTime(int latestStartingTime) {
        this.latestStartingTime = latestStartingTime;
    }

    public int getEarliestEndingTime() {
        return earliestEndingTime;
    }

    public void setEarliestEndingTime(int earliestEndingTime) {
        this.earliestEndingTime = earliestEndingTime;
    }

    public int getLatestEndingTime() {
        return latestEndingTime;
    }

    public void setLatestEndingTime(int latestEndingTime) {
        this.latestEndingTime = latestEndingTime;
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
