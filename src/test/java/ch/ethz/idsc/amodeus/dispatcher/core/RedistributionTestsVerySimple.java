/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.ArtificialScenarioCreator;
import ch.ethz.idsc.amodeus.lp.RedistributionProblemHelper;
import ch.ethz.idsc.amodeus.lp.RedistributionProblemSolver;
import ch.ethz.idsc.amodeus.routing.DistanceFunction;
import ch.ethz.idsc.amodeus.routing.EuclideanDistanceFunction;

public class RedistributionTestsVerySimple {

    private static Link link1;
    private static Link link2;
    private static Link link3;
    private static RoboTaxi roboTaxi;

    @Before
    public void prepare() {
        ArtificialScenarioCreator scenario = new ArtificialScenarioCreator();
        link1 = scenario.linkDown;
        link2 = scenario.linkUp;
        link3 = scenario.linkLeft;
        roboTaxi = new RoboTaxi(null, null, link3, null);
    }

    @Test
    public void test() {
        DistanceFunction distanceFunction = EuclideanDistanceFunction.INSTANCE;
        /** roboTaxi must leave link 1 */
        Map<Link, Set<RoboTaxi>> taxisToGo = new HashMap<>();
        HashSet<RoboTaxi> taxis = new HashSet<>();
        taxis.add(roboTaxi);
        taxisToGo.put(link1, taxis);

        /** free spots available on link 2 */
        Map<Link, Integer> freeSpaces = new HashMap<>();
        freeSpaces.put(link2, 10);

        /** solve it */
        Map<RoboTaxi, Link> solution = //
                flowLayerSolution(taxisToGo, freeSpaces, distanceFunction);

        System.out.println("size: " + solution.size());
        System.out.println(solution.get(roboTaxi).getId());

        /** solution should send roboTaxi to linkup */
        Assert.assertEquals(1, solution.size());
        Assert.assertEquals("linkUp", solution.get(roboTaxi).getId().toString());
    }

    private static Map<RoboTaxi, Link> flowLayerSolution(Map<Link, Set<RoboTaxi>> taxisToGo, Map<Link, Integer> freeSpaces, //
            DistanceFunction distanceFunction) {
        /** creating unitsToMove map */
        Map<Link, Integer> unitsToMove = RedistributionProblemHelper.getFlow(taxisToGo);

        /** solve flow problem */
        RedistributionProblemSolver<Link> parkingLP = new RedistributionProblemSolver<>(unitsToMove, freeSpaces, //
                distanceFunction::getDistance, l -> l.getId().toString(), //
                false, "");
        Map<Link, Map<Link, Integer>> flowSolution = parkingLP.returnSolution();

        /** create roboTaxi movement map */
        return RedistributionProblemHelper.getSolutionCommands(taxisToGo, flowSolution);
    }
}
