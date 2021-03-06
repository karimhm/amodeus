/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.analysis.report.TotalValueAppender;
import ch.ethz.idsc.amodeus.analysis.report.TotalValueIdentifier;
import ch.ethz.idsc.amodeus.analysis.report.TtlValIdent;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetwork;
import ch.ethz.idsc.amodeus.virtualnetwork.core.VirtualNetworkGet;
import ch.ethz.idsc.tensor.io.UserName;
import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.DispatcherConfig;
import ch.ethz.matsim.av.config.operator.GeneratorConfig;
import ch.ethz.matsim.av.config.operator.OperatorConfig;

public class ScenarioParameters implements TotalValueAppender, Serializable {
    public static final int UNDEFINED_INT = -1;
    public static final String UNDEFINED_STRING = "";
    public static final DateFormat DATEFORMAT = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
    // ---
    public static final String DISPATCHPERIODSTRING = "dispatchPeriod";
    public static final String REBALANCINGPERIODSTRING = "rebalancingPeriod";
    public static final String DISTANCEHEURISTICSTRING = "distanceHeuristics";

    public final int populationSize;
    public final int iterations;
    public final int redispatchPeriod;
    public final int rebalancingPeriod;
    public final int virtualNodesCount;

    public final String dispatcher;
    public final String distanceHeuristic;
    public final String virtualNetworkCreator;
    public final String vehicleGenerator;
    public final String networkName;
    public final String user = UserName.get();
    public final String date = DATEFORMAT.format(new Date());

    public ScenarioParameters(ScenarioOptions scenOptions) {
        System.out.println("scenOptions.getSimulationConfigName: " + scenOptions.getSimulationConfigName());

        AVConfigGroup avConfigGroup = new AVConfigGroup();
        Config config = ConfigUtils.loadConfig(scenOptions.getSimulationConfigName(), avConfigGroup);

        OperatorConfig operatorConfig = avConfigGroup.getOperatorConfigs().values().iterator().next();
        DispatcherConfig dispatcherConfig = operatorConfig.getDispatcherConfig();
        SafeConfig safeConfig = SafeConfig.wrap(dispatcherConfig);
        GeneratorConfig generatorConfig = operatorConfig.getGeneratorConfig();

        redispatchPeriod = safeConfig.getInteger(DISPATCHPERIODSTRING, UNDEFINED_INT);
        rebalancingPeriod = safeConfig.getInteger(REBALANCINGPERIODSTRING, UNDEFINED_INT);
        dispatcher = dispatcherConfig.getType();
        vehicleGenerator = generatorConfig.getType();
        Scenario scenario = ScenarioUtils.loadScenario(config);

        distanceHeuristic = safeConfig.getString(DISTANCEHEURISTICSTRING, UNDEFINED_STRING);
        populationSize = scenario.getPopulation().getPersons().values().size();
        virtualNetworkCreator = scenOptions.getString(ScenarioOptionsBase.VIRTUALNETWORKCREATORIDENTIFIER);

        Network network = scenario.getNetwork();
        if (Objects.isNull(network.getName()))
            networkName = "no network name found in network.xml";
        else
            networkName = network.getName();

        VirtualNetwork<Link> virtualNetwork = null;
        try {
            virtualNetwork = VirtualNetworkGet.readDefault(network, scenOptions);
        } catch (IOException e) {
            System.err.println("INFO not able to load virtual network for report");
            e.printStackTrace();
        }

        virtualNodesCount = Objects.isNull(virtualNetwork) //
                ? UNDEFINED_INT : virtualNetwork.getvNodesCount();

        iterations = config.controler().getLastIteration();

    }

    public String getVirtualNetworkDescription() {
        return virtualNodesCount == UNDEFINED_INT //
                ? "no virtual network found" : virtualNodesCount + " virtual nodes.";
    }

    @Override
    public Map<TotalValueIdentifier, String> getTotalValues() {
        Map<TotalValueIdentifier, String> map = new HashMap<>();
        map.put(TtlValIdent.DISPATCHER, dispatcher);
        map.put(TtlValIdent.DISPATCHINGPERIOD, String.valueOf(redispatchPeriod));
        map.put(TtlValIdent.REBALANCEPERIOD, String.valueOf(rebalancingPeriod));
        map.put(TtlValIdent.DISTANCEHEURISTIC, String.valueOf(distanceHeuristic));
        map.put(TtlValIdent.POPULATIONSIZE, String.valueOf(populationSize));
        map.put(TtlValIdent.VIRTUALNODES, String.valueOf(virtualNodesCount));
        map.put(TtlValIdent.VEHICLEGENERATOR, vehicleGenerator);
        map.put(TtlValIdent.TIMESTAMP, date);
        return map;
    }

}
