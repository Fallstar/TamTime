package flying.grub.tamtime.data;

/**
 * Created by fly on 11/29/15.
 */
public class LineStop {
    private Stop stop;
    private Line line;

    public LineStop(Stop stop, Line line) {
        this.stop = stop;
        this.line = line;
    }

    public Stop getStop() {
        return stop;
    }

    public Line getLine() {
        return line;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LineStop lineStop = (LineStop) o;

        if (stop != null ? !stop.equals(lineStop.stop) : lineStop.stop != null) return false;
        return !(line != null ? !line.equals(lineStop.line) : lineStop.line != null);

    }

    @Override
    public int hashCode() {
        int result = stop != null ? stop.hashCode() : 0;
        result = 31 * result + (line != null ? line.hashCode() : 0);
        return result;
    }
}
