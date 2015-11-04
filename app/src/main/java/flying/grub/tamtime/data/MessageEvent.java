package flying.grub.tamtime.data;

/**
 * Created by fly on 9/25/15.
 */
public class MessageEvent {
    public enum Type {
        TIMES_UPDATE,
        LINES_UPDATE,
        EVENT_UPDATE,
        REPORT_UPDATE
    }

    public final Type type;

    public MessageEvent(Type type) {
        this.type = type;
    }
}

