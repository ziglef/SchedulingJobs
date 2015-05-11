package models;

import enums.MachineState;

public class Machine {

    private int id;
    private boolean occupied;
    private MachineState machineState;

    public Machine(int id){
        this.id = id;
        this.occupied = false;
        this.machineState = MachineState.FREE;
    }

    public int getId() {
        return id;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public MachineState getMachineState() {
        return machineState;
    }

    public void setMachineState(MachineState machineState) {
        this.machineState = machineState;
    }
}
