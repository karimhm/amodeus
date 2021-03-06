/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.taxitrip;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.qty.Quantity;

public class ShortestDurationCalculator {

    private final FastLinkLookup fastLinkLookup;
    private final LeastCostPathCalculator leastCostPathCalculator;

    public ShortestDurationCalculator(Network network, MatsimAmodeusDatabase db) {
        leastCostPathCalculator = new FastAStarLandmarksFactory(Runtime.getRuntime().availableProcessors()).createPathCalculator(network, //
                new TravelDisutility() { // free speed travel time
                    @Override
                    public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
                        return getLinkMinimumTravelDisutility(link);
                    }

                    @Override
                    public double getLinkMinimumTravelDisutility(Link link) {
                        return link.getLength() / link.getFreespeed();
                    }
                }, //
                new TravelTime() {
                    @Override
                    public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {
                        return link.getLength() / link.getFreespeed();
                    }
                });
        // fast link lookup
        fastLinkLookup = new FastLinkLookup(network, db);
    }

    public ShortestDurationCalculator(LeastCostPathCalculator lcpc, Network network, MatsimAmodeusDatabase db) {
        this.leastCostPathCalculator = lcpc;
        // fast link lookup
        fastLinkLookup = new FastLinkLookup(network, db);
    }

    public Scalar computePathTime(TaxiTrip taxiTrip) {
        return Quantity.of(computePath(taxiTrip).travelTime, SI.SECOND);
    }

    public Path computePath(TaxiTrip taxiTrip) {
        return computePath(taxiTrip.pickupLoc, taxiTrip.dropoffLoc);
    }

    public Path computePath(Tensor pickupLoc, Tensor dropoffLoc) {
        return computePath( //
                fastLinkLookup.linkFromWGS84(TensorCoords.toCoord(pickupLoc)), //
                fastLinkLookup.linkFromWGS84(TensorCoords.toCoord(dropoffLoc)));
    }

    /** function is used in amodtaxi
     * 
     * @param pickupLink
     * @param dropOffLink
     * @return */
    public Path computePath(Link pickupLink, Link dropOffLink) {
        return leastCostPathCalculator.calcLeastCostPath(pickupLink.getFromNode(), dropOffLink.getToNode(), 1, null, null);
    }

}
