import java.util.*;

/**
 * Represents a single elevator with directional commitment.
 * Handles hall calls, interior requests, and per-tick movement.
 */
public class Elevator {
    private final int numFloors;
    private int current;
    private double pos;
    private Direction dir = Direction.IDLE;
    private Door door = Door.CLOSED;
    private int dwellTicks = 0;

    // Floors requested in each direction
    private final NavigableSet<Integer> up = new TreeSet<>();
    private final NavigableSet<Integer> down = new TreeSet<>(Comparator.reverseOrder());

    // Map of pending destinations for each origin floor
    private final Map<Integer, List<Integer>> pendingDestinations = new HashMap<>();

    // Constants
    private final int DWELL_TICKS = 2; //How many ticks to dwell at a floor
    private final double FLOORS_PER_TICK = 1.0; //Speed of the elevator in floors per tick

    /* Constructor */
    public Elevator(int numFloors, int startFloor) {
        this.numFloors = numFloors;
        this.current = clamp(startFloor, 0, numFloors - 1);
        this.pos = this.current;
    }

    /* Add a hall call from origin to dest */
    public void addHallCall(int origin, int dest) {
        origin = clamp(origin, 0, numFloors - 1);
        dest   = clamp(dest,   0, numFloors - 1);
        if (origin == dest) return;

        pendingDestinations.computeIfAbsent(origin, k -> new ArrayList<>()).add(dest);

        if (dir == Direction.IDLE) {
            if (origin > current) up.add(origin);
            else if (origin < current) down.add(origin);
            return;
        }
        if (dir == Direction.UP) {
            if (origin >= current) up.add(origin);
            else down.add(origin);
        } else { // DOWN
            if (origin <= current) down.add(origin);
            else up.add(origin);
        }
    }

    public void step() {
        if (dwellTicks > 0) {
            dwellTicks--;
            if (dwellTicks == 0) door = Door.CLOSED;
            return;
        }

        if (dir == Direction.IDLE) {
            if (!up.isEmpty() && down.isEmpty()) dir = Direction.UP;
            else if (up.isEmpty() && !down.isEmpty()) dir = Direction.DOWN;
            else if (!up.isEmpty() && !down.isEmpty()) {
                int nextUp = up.first();
                int nextDown = down.first();
                int dUp = Math.abs(nextUp - current);
                int dDown = Math.abs(current - nextDown);
                dir = (dUp <= dDown) ? Direction.UP : Direction.DOWN;
            } else return;
        }

        Integer target = (dir == Direction.UP) ? (up.isEmpty() ? null : up.first())
                : (down.isEmpty() ? null : down.first());
        if (target == null) {
            if (dir == Direction.UP && !down.isEmpty()) { dir = Direction.DOWN; target = down.first(); }
            else if (dir == Direction.DOWN && !up.isEmpty()) { dir = Direction.UP; target = up.first(); }
            else { dir = Direction.IDLE; return; }
        }

        if (isAtFloor(target)) {
            arriveAt(target);
            return;
        }

        door = Door.CLOSED;
        double step = FLOORS_PER_TICK;
        if (dir == Direction.UP) pos = Math.min(pos + step, target);
        else pos = Math.max(pos - step, target);
        current = (int)Math.round(pos);
    }

    private void arriveAt(int floor) {
        pos = floor; current = floor;
        if (dir == Direction.UP) up.pollFirst();
        else if (dir == Direction.DOWN) down.pollFirst();

        door = Door.OPEN;
        dwellTicks = DWELL_TICKS;

        List<Integer> dests = pendingDestinations.remove(floor);
        if (dests != null) {
            for (int d : dests) {
                if (d == floor) continue;
                if (d > floor) up.add(d);
                else if (d < floor) down.add(d);
            }
        }
    }

    private boolean isAtFloor(int f) { return Math.abs(pos - f) < 1e-9; }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public String state() {
        return String.format("floor=%d dir=%s door=%s up=%s down=%s dwell=%d",
                current, dir, door, up, down, dwellTicks);
    }
}
