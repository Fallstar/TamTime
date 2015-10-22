package flying.grub.tamtime.data;

public enum ReportType {
    CONTROLE_Q(1), CONTROLE_T(2), INCIDENT(3), AUTRE(4);
    private int value;

    ReportType(int value) {
        this.value = value;
    }

    public int getValue() {
       return this.value;
    }

    public static ReportType reportFromId(int id) {
        ReportType res;
        switch (id) {
            case 1:
                res = ReportType.CONTROLE_Q;
                break;
            case 2:
                res = ReportType.CONTROLE_T;
                break;
            case 3:
                res = ReportType.INCIDENT;
                break;
            case 4:
                res = ReportType.AUTRE;
                break;
            default:
                res = null;
                break;
        }
        return res;
    }

    public static ReportType reportFromPosition(int pos) {
        return reportFromId(pos +1);
    }

    public int getValueForString() {
        return this.value -1;
    }
}
