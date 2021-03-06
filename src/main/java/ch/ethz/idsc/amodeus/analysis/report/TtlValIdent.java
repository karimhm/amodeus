/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.analysis.report;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TtlValIdent implements TotalValueIdentifier {
    // General, Dispatchers, Generators
    TIMESTAMP("timeStamp"), //
    DISPATCHER("dispatcher"), //
    VEHICLEGENERATOR("vehicleGenerator"), //
    DISTANCEHEURISTIC("distanceHeuristic"), //
    REBALANCEPERIOD("rebalancePeriod"), //
    DISPATCHINGPERIOD("dispatchingPeriod"), //
    VIRTUALNODES("virtualNodes"), //
    VIRTUALNETWORKCREATOR("virtualNetworkCreator"), //
    TOTALVEHICLES("totalVehicles"), //
    TOTALREQUESTS("totalRequests"), //
    POPULATIONSIZE("populationSize"), //

    // Wait Times
    WAITTMEA("MeanWaitingTime"), //
    WAITTQU1("WaitTimeQuantile1"), //
    WAITTQU2("WaitTimeQuantile2"), //
    WAITTQU3("WaitTimeQuantile3"), //
    WAITTMAX("WaitTimeMax"), //

    // Drive Times
    DRIVETMEA("MeanDriveTime"), //
    DRIVETQU1("DriveTimeQuantile1"), //
    DRIVETQU2("DriveTimeQuantile2"), //
    DRIVETQU3("DriveTimeQuantile3"), //
    DRIVETMAX("DriveTimeMax"), //

    // Total Travel Times
    TRAVELTMEA("MeanTTravelTime"), //
    TRAVELTQU1("TTravelTimeQuantile1"), //
    TRAVELTQU2("TTravelTimeQuantile2"), //
    TRAVELTQU3("TTravelTimeQuantile3"), //
    TRAVELTMAX("TTravelTimeMax"), //

    // Distances
    TOTALROBOTAXIDISTANCE("TotalRoboTaxiDistance"), //
    TOTALROBOTAXIDISTANCEREB("totalRoboTaxiDistanceReb"), //
    TOTALROBOTAXIDISTANCEPICKU("totalRoboTaxiDistancePicku"), //
    TOTALROBOTAXIDISTANCEWTCST("totalRoboTaxiDistanceWtCst"), //
    AVGTRIPDISTANCE("averageTripDistance"), //
    DISTANCERATIO("distanceRatio"), //
    OCCUPANCYRATIO("occupancyRatio"), //

    // SHARED Values
    REQUESTSSHAREDNUMBERS("requestSharedNumbers");

    private final String identifier;

    private TtlValIdent(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    // ---
    public static Set<String> getAllIdentifiers() {
        return Stream.of(values()) //
                .map(TtlValIdent::getIdentifier) //
                .collect(Collectors.toSet());
    }

    public static boolean contains(TotalValueIdentifier totalValueIdentifier) {
        return getAllIdentifiers().contains(totalValueIdentifier.getIdentifier());
    }
}
