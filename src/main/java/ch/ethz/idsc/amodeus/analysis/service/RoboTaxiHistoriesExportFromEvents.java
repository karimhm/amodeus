/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.service;

import java.io.File;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;

import ch.ethz.idsc.amodeus.analysis.AnalysisSummary;
import ch.ethz.idsc.amodeus.analysis.element.AnalysisExport;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

public class RoboTaxiHistoriesExportFromEvents implements AnalysisExport {

    private static final String VEHICLE_HISTORY_CSV = "vehicleHistory.csv";

    private final Network network;
    private final String eventFile;

    public RoboTaxiHistoriesExportFromEvents(Network network, Config config) {
        this.network = network;
        eventFile = config.controler().getOutputDirectory() + "/output_events.xml.gz";
    }

    @Override
    public void summaryTarget(AnalysisSummary analysisSummary, File relativeDirectory, ColorDataIndexed colorDataIndexed) {
        try {
            ConvertAVTracesFromEvents.write(network, relativeDirectory.getAbsolutePath() + "/" + VEHICLE_HISTORY_CSV, eventFile);
        } catch (Exception e) {
            System.err.println("It was not possible to create the " + VEHICLE_HISTORY_CSV);
        }
    }

}
