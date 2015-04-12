package org.controlsfx.control.table;

import java.util.function.UnaryOperator;

/**
 * Created by Thomas on 3/15/2015.
 */
public final class OD {

    private final String orig;
    private final String dest;

    public static OD of(String orig, String dest) {
        return new OD(orig, dest);
    }
    private OD(String orig, String dest) {
        this.orig = orig;
        this.dest = dest;
    }
    public String getOrig() {
        return orig;
    }
    public String getDest() {
        return dest;
    }

    public OD convert(ODConversion odConversion) {
        return odConversion.convertFx.apply(this);
    }

    public static enum ODConversion {

        NONE(od -> od),
        HALF_ALPHA(od -> od.orig.compareTo(od.dest) > 0 ? OD.of(od.dest,od.orig) : od),
        REVERSE_ALPHA(od -> od.orig.compareTo(od.dest) > 0 ?  od : OD.of(od.dest,od.orig));

        private final UnaryOperator<OD> convertFx;
        private ODConversion(UnaryOperator<OD> convertFx) {
            this.convertFx = convertFx;
        }
    }
}
