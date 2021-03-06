/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.util;

import java.util.Collection;
import java.util.Map;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxi;
import ch.ethz.idsc.amodeus.dispatcher.core.UniversalDispatcher;
import ch.ethz.idsc.amodeus.matsim.SafeConfig;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.matsim.av.passenger.AVRequest;

public class ConfigurableBipartiteMatcher extends BipartiteMatcherInternal {

    private final AbstractRoboTaxiDestMatcher globalBipartiteMatcher;
    private final Boolean hungarian;

    /** Allows to instantiate a configurable bipartite matching algorithm via the av.xml file, there are two options:
     * - 1 (default option): the Hungarian method is used, this is chosen if no specification is given in av.xml or the specification
     * 
     * <dispatcher strategy="GlobalBipartiteMatchingDispatcher">
     * <param name="matchingAlgorithm" value="HUNGARIAN" />
     * 
     * -2: solution of the assignment problem via Integer Linear Program, for this option the av.xml file should look as follows
     * <dispatcher strategy="GlobalBipartiteMatchingDispatcher">
     * <param name="matchingAlgorithm" value="ILP" />
     * <param name="matchingWeight" value="[1.0,1.0,1.0]" />
     * 
     * The values are retrieved via @param safeConfig, other parameters necessary for instantiation are
     * the network @param network, and the distance function @param distanceFunction */
    public ConfigurableBipartiteMatcher(Network network, GlobalBipartiteCost cost, SafeConfig safeConfig) {
        super(network);
        String matchingAlg = safeConfig.getString("matchingAlgorithm", "HUNGARIAN");
        switch (matchingAlg) {
        case "HUNGARIAN":
            hungarian = true;
            globalBipartiteMatcher = new GlobalBipartiteMatching(cost);
            break;
        case "ILP":
            hungarian = false;
            globalBipartiteMatcher = new GlobalBipartiteMatchingILP(cost, safeConfig);
            break;
        default:
            // hungarian = null;
            // globalBipartiteMatcher = null;
            throw new RuntimeException("An invalid option for the matching algorithm was chosen. " + matchingAlg);
        }
    }

    @Override
    public Map<RoboTaxi, AVRequest> getGBPMatch(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network) {
        if (hungarian)
            return hungarianMatch(universalDispatcher, roboTaxis, requests, distanceFunction, network);
        return integerLinearProgramMatch(universalDispatcher, roboTaxis, requests, distanceFunction, network);
    }

    private Map<RoboTaxi, AVRequest> hungarianMatch(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network) {
        /** reduction of problem size with kd-tree, helps to downsize problems where n << m or m>> n
         * for n number of available taxis and m number of available requests */
        Map<RoboTaxi, AVRequest> gbpMatch = globalBipartiteMatcher.match(roboTaxis, requests);
        /** prevent cycling an assignment is only updated if the new distance is smaller than the
         * old distance */
        return CyclicSolutionPreventer.apply(gbpMatch, universalDispatcher, accDstFctn);
    }

    private Map<RoboTaxi, AVRequest> integerLinearProgramMatch(UniversalDispatcher universalDispatcher, //
            Collection<RoboTaxi> roboTaxis, /** <- typically universalDispatcher.getDivertableRoboTaxis() */
            Collection<AVRequest> requests, /** <- typically universalDispatcher.getAVRequests() */
            DistanceFunction distanceFunction, Network network) {
        /** reduction of problem size with kd-tree, helps to downsize problems where n << m or m>> n
         * for n number of available taxis and m number of available requests */
        return globalBipartiteMatcher.match(roboTaxis, requests);
        /** prevent cycling an assignment is only updated if the new distance is smaller than the
         * old distance */
    }

}
