/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;
import org.matsim.contrib.dvrp.tracker.TaskTracker;
import org.matsim.contrib.dvrp.util.LinkTimePair;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDriveTaskTracker;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.SimulationObjectCompiler;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.data.AVVehicle;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.generator.AVGenerator;
import ch.ethz.matsim.av.passenger.AVRequest;
import ch.ethz.matsim.av.plcpc.ParallelLeastCostPathCalculator;
import ch.ethz.refactoring.schedule.AmodeusDriveTask;
import ch.ethz.refactoring.schedule.AmodeusDropoffTask;
import ch.ethz.refactoring.schedule.AmodeusPickupTask;
import ch.ethz.refactoring.schedule.AmodeusStayTask;

/** purpose of {@link UniversalDispatcher} is to collect and manage
 * {@link AVRequest}s alternative implementation of {@link AVDispatcher};
 * supersedes {@link AbstractDispatcher}. */
public abstract class UniversalDispatcher extends BasicUniversalDispatcher {
    private final Map<AVRequest, RoboTaxi> pickupRegister = new HashMap<>();
    private final Map<AVRequest, RoboTaxi> rqstDrvRegister = new HashMap<>();
    private final Map<AVRequest, RoboTaxi> periodFulfilledRequests = new HashMap<>();
    private final Set<AVRequest> periodAssignedRequests = new HashSet<>();
    private final Set<AVRequest> periodPickedUpRequests = new HashSet<>();

    protected UniversalDispatcher(Config config, OperatorConfig operatorConfig, //
            TravelTime travelTime, ParallelLeastCostPathCalculator parallelLeastCostPathCalculator, //
            EventsManager eventsManager, MatsimAmodeusDatabase db) {
        super(eventsManager, config, operatorConfig, travelTime, //
                parallelLeastCostPathCalculator, db);
    }

    // ===================================================================================
    // Methods to use EXTERNALLY in derived dispatchers

    /** @return {@link AVRequest}s currently not assigned to a vehicle */
    protected synchronized final List<AVRequest> getUnassignedAVRequests() {
        return pendingRequests.stream() //
                .filter(r -> !pickupRegister.containsKey(r)) //
                .collect(Collectors.toList());
    }

    /** @return divertable {@link RoboTaxi}s which currently not on a pickup drive */
    protected final Collection<RoboTaxi> getDivertableUnassignedRoboTaxis() {
        Collection<RoboTaxi> divertableUnassignedRoboTaxis = getDivertableRoboTaxis().stream() //
                .filter(rt -> !pickupRegister.containsValue(rt)) //
                .collect(Collectors.toList());
        GlobalAssert.that(divertableUnassignedRoboTaxis.stream().noneMatch(pickupRegister::containsValue));
        GlobalAssert.that(divertableUnassignedRoboTaxis.stream().allMatch(RoboTaxi::isWithoutCustomer));
        return divertableUnassignedRoboTaxis;
    }

    /** @return {@link Collection<RoboTaxi>}s which is in stay task (idling) */
    protected final Collection<RoboTaxi> getStayingTaxi() {
        return getDivertableUnassignedRoboTaxis().stream() //
                .filter(RoboTaxi::isInStayTask) //
                .collect(Collectors.toList());
    }

    /** @return immutable and inverted copy of pickupRegister, displays which
     *         vehicles are currently scheduled to pickup which request */
    protected final Map<RoboTaxi, AVRequest> getPickupRoboTaxis() {
        Map<RoboTaxi, AVRequest> pickupPairs = pickupRegister.entrySet().stream() //
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        GlobalAssert.that(pickupPairs.keySet().stream().allMatch(rt -> rt.getStatus().equals(RoboTaxiStatus.DRIVETOCUSTOMER)));
        return pickupPairs;
    }

    /** Diverts {@link RoboTaxi} to {@link Link} of {@link AVRequest} and adds pair
     * to pickupRegister. If the {@link RoboTaxi} was scheduled to pickup another
     * {@link AVRequest}, then this pair is silently revmoved from the pickup
     * register.
     * 
     * @param roboTaxi
     * @param avRequest */
    public final void setRoboTaxiPickup(RoboTaxi roboTaxi, AVRequest avRequest) {
        GlobalAssert.that(roboTaxi.isWithoutCustomer());
        GlobalAssert.that(pendingRequests.contains(avRequest));

        /** for some dispatchers, reassignment is permanently invoked again, the
         * {@link RoboTaxi} should appear under only at the time step of assignment */
        if (!pickupRegister.containsKey(avRequest))
            periodAssignedRequests.add(avRequest);

        // 1) enter information into pickup table
        if (pickupRegister.containsValue(roboTaxi)) {
            AVRequest toRemove = pickupRegister.entrySet().stream()//
                    .filter(e -> e.getValue().equals(roboTaxi)).findAny().get().getKey();
            pickupRegister.remove(toRemove); // remove AVRequest/RoboTaxi pair served before by roboTaxi
            pickupRegister.remove(avRequest); // remove AVRequest/RoboTaxi pair corresponding to avRequest
        }
        pickupRegister.put(avRequest, roboTaxi); // add new pair
        GlobalAssert.that(pickupRegister.size() == pickupRegister.values().stream().distinct().count());

        // 2) set vehicle diversion
        setRoboTaxiDiversion(roboTaxi, avRequest.getFromLink(), RoboTaxiStatus.DRIVETOCUSTOMER);
    }

    // ===================================================================================
    // INTERNAL Methods, do not call from derived dispatchers.

    /** For {@link UniversalDispatcher}, {@link VehicleMaintainer} internal use only.
     * Use {@link UniversalDispatcher#setRoboTaxiPickup} or
     * {@link setRoboTaxiRebalance} from dispatchers. Assigns new destination to
     * vehicle, if vehicle is already located at destination, nothing happens. In
     * one pass of {@redispatch(...)} in {@VehicleMaintainer}, the function
     * setVehicleDiversion(...) may only be invoked once for a single
     * {@link RoboTaxi} vehicle
     *
     * @param roboTaxi {@link RoboTaxi} supplied with a getFunction,e.g.,
     *            {@link this.getDivertableRoboTaxis}
     * @param destination {@link Link} the {@link RoboTaxi} should be diverted to
     * @param status {@link} the {@link RoboTaxiStatus} the {@link RoboTaxi}
     *            has after the diversion, depends if used from
     *            {@link UniversalDispatcher#setRoboTaxiPickup} or {@link setRoboTaxiRebalance} */
    final void setRoboTaxiDiversion(RoboTaxi roboTaxi, Link destination, RoboTaxiStatus status) {
        /** update {@link RoboTaxiStatus} of {@link RoboTaxi} */
        GlobalAssert.that(roboTaxi.isWithoutCustomer());
        GlobalAssert.that(roboTaxi.isWithoutDirective());
        roboTaxi.setStatus(status);

        routingForDiversion(roboTaxi, destination, false);
    }

    /** this function will re-route the taxi if it is not in stay task (for
     * congestion relieving purpose) */
    protected final void reRoute(RoboTaxi roboTaxi) {
        if (!roboTaxi.isInStayTask() && roboTaxi.canReroute())
            routingForDiversion(roboTaxi, roboTaxi.getCurrentDriveDestination(), true);
    }

    // the function below is for internal use only!
    private final void routingForDiversion(RoboTaxi roboTaxi, Link destination, boolean reRoute) {
        /** update {@link Schedule} of {@link RoboTaxi} */
        // the 3rd parameter "reRoute" is added for re-routing the taxi to avoid
        // congestion
        final Schedule schedule = roboTaxi.getSchedule();
        Task task = schedule.getCurrentTask();
        new RoboTaxiTaskAdapter(task) {
            @Override
            public void handle(AmodeusDriveTask avDriveTask) {
                if (reRoute || !avDriveTask.getPath().getToLink().equals(destination)) { // ignore when vehicle is
                                                                                         // already going there
                    FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer( //
                            roboTaxi.getDivertableLocation(), destination, roboTaxi.getDivertableTime());
                    if (reRoute)
                        roboTaxi.assignDirective(new DriveVehicleRerouteDirective(futurePathContainer, roboTaxi));
                    else
                        roboTaxi.assignDirective(new DriveVehicleDiversionDirective(roboTaxi, destination, futurePathContainer));
                } else
                    roboTaxi.assignDirective(EmptyDirective.INSTANCE);
            }

            @Override
            public void handle(AmodeusStayTask avStayTask) {
                // if (!reRoute) {/** a staying vehicle cannot be rerouted */
                if (!avStayTask.getLink().equals(destination)) { // ignore request where location == target
                    FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer( //
                            roboTaxi.getDivertableLocation(), destination, roboTaxi.getDivertableTime());
                    roboTaxi.assignDirective(new StayVehicleDiversionDirective(roboTaxi, destination, futurePathContainer));
                } else
                    roboTaxi.assignDirective(EmptyDirective.INSTANCE);
                // }
            }
        };
    }

    /** Function called from {@link UniversalDispatcher#executePickups} if a
     * {@link RoboTaxi} scheduled for pickup has reached the
     * {@link AVRequest#pickupLink} of the {@link AVRequest}.
     * 
     * @param roboTaxi
     * @param avRequest */
    private synchronized final void setAcceptRequest(RoboTaxi roboTaxi, AVRequest avRequest) {
        roboTaxi.setStatus(RoboTaxiStatus.DRIVEWITHCUSTOMER);
        roboTaxi.setCurrentDriveDestination(avRequest.getToLink());

        /** request not pending anymore */
        boolean statusPen = pendingRequests.remove(avRequest);
        GlobalAssert.that(statusPen);

        /** request not during pickup anymore */
        RoboTaxi formerpckp = pickupRegister.remove(avRequest);
        GlobalAssert.that(roboTaxi == formerpckp);

        /** now during drive */
        RoboTaxi formerrqstDrv = rqstDrvRegister.put(avRequest, roboTaxi);
        GlobalAssert.that(Objects.isNull(formerrqstDrv));

        /** ensure recorded in {@link SimulationObject} */
        periodPickedUpRequests.add(avRequest);
        consistencySubCheck();

        final Schedule schedule = roboTaxi.getSchedule();
        GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule));

        final double endPickupTime = getTimeNow() + pickupDurationPerStop;
        FuturePathContainer futurePathContainer = futurePathFactory.createFuturePathContainer(avRequest.getFromLink(), avRequest.getToLink(), endPickupTime);

        roboTaxi.assignDirective(new AcceptRequestDirective(roboTaxi, avRequest, futurePathContainer, getTimeNow(), dropoffDurationPerStop));

        ++total_matchedRequests;
    }

    /** Function called from {@link UniversalDispatcher#executeDropoffs} if a
     * {@link RoboTaxi} scheduled for dropoff has reached the
     * {@link AVRequest#dropoffLink} of the {@link AVRequest}.
     * 
     * @param roboTaxi
     * @param avRequest */
    private synchronized final void setPassengerDropoff(RoboTaxi roboTaxi, AVRequest avRequest) {
        RoboTaxi former = rqstDrvRegister.remove(avRequest);
        GlobalAssert.that(roboTaxi == former);

        /** save avRequests which are matched for one publishPeriod to ensure requests
         * appear in {@link SimulationObject}s */
        periodFulfilledRequests.put(avRequest, roboTaxi);

        /** check that current task is last task in schedule */
        final Schedule schedule = roboTaxi.getSchedule();
        GlobalAssert.that(schedule.getCurrentTask() == Schedules.getLastTask(schedule));
    }

    protected final boolean isInPickupRegister(RoboTaxi robotaxi) {
        return pickupRegister.containsValue(robotaxi);
    }

    /* package */ final boolean removeFromPickupRegisters(AVRequest avRequest) {
        return Objects.isNull(pickupRegister.remove(avRequest));
    }

    /** @param avRequest
     * @return {@link RoboTaxi} assigned to given avRequest, or empty if no taxi is
     *         assigned to avRequest Used by BipartiteMatching in
     *         euclideanNonCyclic, there a comparison to the old av assignment is
     *         needed */
    public final Optional<RoboTaxi> getPickupTaxi(AVRequest avRequest) {
        return Optional.ofNullable(pickupRegister.get(avRequest));
    }

    /** complete all matchings if a {@link RoboTaxi} has arrived at the fromLink of
     * an {@link AVRequest} */
    @Override
    final void executePickups() {
        Map<AVRequest, RoboTaxi> pickupRegisterCopy = new HashMap<>(pickupRegister);
        for (Entry<AVRequest, RoboTaxi> entry : pickupRegisterCopy.entrySet()) {
            AVRequest avRequest = entry.getKey();
            GlobalAssert.that(pendingRequests.contains(avRequest));
            RoboTaxi pickupVehicle = entry.getValue();
            Link pickupVehicleLink = pickupVehicle.getDivertableLocation();
            boolean isOk = pickupVehicle.getSchedule().getCurrentTask() == Schedules.getLastTask(pickupVehicle.getSchedule());
            if (avRequest.getFromLink().equals(pickupVehicleLink) && isOk)
                setAcceptRequest(pickupVehicle, avRequest);
        }
    }

    /** complete all matchings if a {@link RoboTaxi} has arrived at the toLink of an
     * {@link AVRequest} */
    @Override
    final void executeDropoffs() {
        Map<AVRequest, RoboTaxi> requestRegisterCopy = new HashMap<>(rqstDrvRegister);
        for (Entry<AVRequest, RoboTaxi> entry : requestRegisterCopy.entrySet())
            if (Objects.nonNull(entry.getValue())) {
                AVRequest avRequest = entry.getKey();
                RoboTaxi dropoffVehicle = entry.getValue();
                Link dropoffVehicleLink = dropoffVehicle.getDivertableLocation();
                boolean isOk = dropoffVehicle.getSchedule().getCurrentTask() == Schedules.getLastTask(dropoffVehicle.getSchedule());
                if (avRequest.getToLink().equals(dropoffVehicleLink) && isOk)
                    setPassengerDropoff(dropoffVehicle, avRequest);
            }
    }

    /** function stops {@link RoboTaxi} which are still heading towards an
     * {@link AVRequest} but another {@link RoboTaxi} was scheduled to pickup this
     * {@link AVRequest} in the meantime */
    @Override
    /* package */ final void stopAbortedPickupRoboTaxis() {

        /** stop vehicles still driving to a request but other taxi serving that request
         * already */
        getRoboTaxis().stream()//
                .filter(rt -> rt.getStatus().equals(RoboTaxiStatus.DRIVETOCUSTOMER)) //
                .filter(rt -> !pickupRegister.containsValue(rt)) //
                .filter(RoboTaxi::isWithoutCustomer) //
                .filter(RoboTaxi::isWithoutDirective) //
                .forEach(rt -> setRoboTaxiDiversion(rt, rt.getDivertableLocation(), RoboTaxiStatus.REBALANCEDRIVE));
        GlobalAssert.that(pickupRegister.size() <= pendingRequests.size());
    }

    /** Consistency checks to be called by {@link RoboTaxiHandler.consistencyCheck}
     * in each iteration. */
    @Override
    protected final void consistencySubCheck() {
        GlobalAssert.that(pickupRegister.size() <= pendingRequests.size());

        /** containment check pickupRegister and pendingRequests */
        pickupRegister.keySet().forEach(r -> GlobalAssert.that(pendingRequests.contains(r)));

        /** ensure no robotaxi is scheduled to pickup two requests */
        GlobalAssert.that(pickupRegister.size() == pickupRegister.values().stream().distinct().count());
    }

    @Override
    /* package */ final void insertRequestInfo(SimulationObjectCompiler simulationObjectCompiler) {
        /** pickup register must be after pending requests, request is pending from
         * moment it appears until it is picked up, this period may contain several not
         * connected pickup periods (cancelled pickup attempts) */
        simulationObjectCompiler.insertRequests(pendingRequests, RequestStatus.REQUESTED);
        simulationObjectCompiler.insertRequests(pickupRegister.keySet(), RequestStatus.PICKUPDRIVE);
        simulationObjectCompiler.insertRequests(rqstDrvRegister.keySet(), RequestStatus.DRIVING);

        /** the request is only contained in these three maps durnig 1 time step, which
         * is why they must be inserted after the first three which (potentially) are
         * for multiple time steps. */
        simulationObjectCompiler.insertRequests(periodAssignedRequests, RequestStatus.ASSIGNED);
        simulationObjectCompiler.insertRequests(periodPickedUpRequests, RequestStatus.PICKUP);
        simulationObjectCompiler.insertRequests(periodFulfilledRequests.keySet(), RequestStatus.DROPOFF);

        /** insert information of association of {@link RoboTaxi}s and {@link AVRequest}s */
        simulationObjectCompiler.addRequestRoboTaxiAssoc(pickupRegister);
        simulationObjectCompiler.addRequestRoboTaxiAssoc(rqstDrvRegister);
        simulationObjectCompiler.addRequestRoboTaxiAssoc(periodFulfilledRequests);

        periodFulfilledRequests.clear();
        periodAssignedRequests.clear();
        periodPickedUpRequests.clear();
    }

    @Override
    final void redispatchInternal(double now) {
        // deliberately empty
    }

    @Override
    final void executeRedirects() {
        // deliberately empty
    }

    /** adding a vehicle during setup of simulation, handeled by {@link AVGenerator} */
    @Override
    public final void addVehicle(AVVehicle vehicle) {
        super.addVehicle(vehicle, RoboTaxiUsageType.SINGLEUSED);
    }

    /** updates the divertable locations, i.e., locations from which a
     * {@link RoboTaxi} can deviate its path according to the current Tasks in the
     * MATSim engine */
    @Override
    protected final void updateDivertableLocations() {
        for (RoboTaxi roboTaxi : getRoboTaxis()) {
            GlobalAssert.that(roboTaxi.isWithoutDirective());
            Schedule schedule = roboTaxi.getSchedule();
            new RoboTaxiTaskAdapter(schedule.getCurrentTask()) {
                @Override
                public void handle(AmodeusDriveTask avDriveTask) {
                    // for empty cars the drive task is second to last task
                    TaskTracker taskTracker = avDriveTask.getTaskTracker();
                    AmodeusDriveTaskTracker onlineDriveTaskTracker = (AmodeusDriveTaskTracker) taskTracker;
                    LinkTimePair linkTimePair = onlineDriveTaskTracker.getSafeDiversionPoint();
                    roboTaxi.setDivertableLinkTime(linkTimePair); // contains null check
                    roboTaxi.setCurrentDriveDestination(avDriveTask.getPath().getToLink());
                    GlobalAssert.that(ScheduleUtils.isNextToLastTask(schedule, avDriveTask) != roboTaxi.getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
                }

                @Override
                public void handle(AmodeusPickupTask avPickupTask) {
                    GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
                }

                @Override
                public void handle(AmodeusDropoffTask avDropOffTask) {
                    GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
                }

                @Override
                public void handle(AmodeusStayTask avStayTask) {
                    // for empty vehicles the current task has to be the last task
                    if (ScheduleUtils.isLastTask(schedule, avStayTask) && !isInPickupRegister(roboTaxi)) {
                        GlobalAssert.that(avStayTask.getBeginTime() <= getTimeNow());
                        GlobalAssert.that(avStayTask.getLink() != null);
                        roboTaxi.setDivertableLinkTime(new LinkTimePair(avStayTask.getLink(), getTimeNow()));
                        roboTaxi.setCurrentDriveDestination(avStayTask.getLink());
                        roboTaxi.setStatus(RoboTaxiStatus.STAY);
                    }
                }
            };
        }
    }
}
