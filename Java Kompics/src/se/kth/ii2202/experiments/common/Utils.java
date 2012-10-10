package se.kth.ii2202.experiments.common;

/**
 *
 * @author carbone
 */
public class Utils {

    public static boolean isLargerThan(int ts, int wr, int ts2, int wr2) {
        if (ts > ts2) {
            return true;
        } else {
            return wr > wr2;
        }
    }

    public static int max(int val1, int val2) {
        if (val1 >= val2) {
            return val1;
        }
        return val2;
    }
}
