// Simple single-car elevator system wrapper

public class ElevatorSystem {
    private final Elevator car;

    public ElevatorSystem(int numFloors, int startFloor) {
        this.car = new Elevator(numFloors, startFloor);
    }

    public void requestRide(int origin, int dest) {
        car.addHallCall(origin, dest);
    }

    // Advance simulation one tick
    public void step() { car.step(); }

    // Get current state of the elevator as a string
    public String state() { return car.state(); }

    // Example
    public static void main(String[] args) {
        ElevatorSystem sys = new ElevatorSystem(21, 0);
        sys.requestRide(0, 7); // pickup at 0, drop 7
        sys.requestRide(3, 1); // pickup at 3, drop 1

        for (int t = 0; t < 30; t++) {
            System.out.printf("t=%02d  %s%n", t, sys.state());
            sys.step();
        }
    }
}
