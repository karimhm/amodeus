/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

/* package */ class InfoLine {

    private static final int COMPARELENGTH = 16;
    /** positive values determine the period, negative values or 0 will disable
     * the printout */
    private int infoLinePeriod = 0;
    private String previousInfoMarker = "";

    public InfoLine(int infoLinePeriod) {
        this.infoLinePeriod = infoLinePeriod;
    }

    /** @param infoLinePeriod */
    public final void setInfoLinePeriod(int infoLinePeriod) {
        this.infoLinePeriod = infoLinePeriod;
    }

    /** prints infoLine to console and simulation
     * 
     * @param infoLine
     *            string to be printed
     * @param now
     *            current time */
    /* package */ void updateInfoLine(String infoLine, double now) {
        if (0 < infoLinePeriod && Math.round(now) % infoLinePeriod == 0) {
            String marker = infoLine.substring(COMPARELENGTH);
            if (!marker.equals(previousInfoMarker)) {
                previousInfoMarker = marker;
                System.out.println(infoLine);
            }
        }
    }
}
