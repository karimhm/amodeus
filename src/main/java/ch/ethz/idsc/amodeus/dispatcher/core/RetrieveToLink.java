/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodeus.dispatcher.core;

import java.util.Optional;

import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.dvrp.schedule.Schedule;
import org.matsim.contrib.dvrp.schedule.Schedules;
import org.matsim.contrib.dvrp.schedule.Task;

import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourse;
import ch.ethz.idsc.amodeus.dispatcher.shared.SharedCourseAccess;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.refactoring.schedule.AmodeusDriveTask;
import ch.ethz.refactoring.schedule.AmodeusTaskType;

/*package*/ enum RetrieveToLink {
    ;

    /* package */ static Optional<Link> forShared(RoboTaxi roboTaxi, double now) {

        GlobalAssert.that(!NextCourseIsRedirectToCurrentLink.check(roboTaxi));

        Optional<SharedCourse> currentCourse = SharedCourseAccess.getStarter(roboTaxi);
        final Schedule schedule = roboTaxi.getSchedule();
        final Task currentTask = schedule.getCurrentTask();
        boolean isOnLastTask = currentTask == Schedules.getLastTask(schedule);
        boolean isSecondLastTask = ScheduleUtils.isNextToLastTask(schedule, currentTask);
        boolean taskEndsNow = LastTimeStep.check(currentTask, now, SharedUniversalDispatcher.SIMTIMESTEP);

        if (currentCourse.isPresent()) {
            if (roboTaxi.isWithoutDirective()) {

                boolean divert = false;
                // FIRST: We reach the point where the Robo Taxi does not know what to do based on the schedule
                // We have a current Course but the task is close to the end or already on the last task
                if (isOnLastTask || (isSecondLastTask && taskEndsNow))
                    divert = true;

                // SECOND: The course Of the Menu Changed
                // SECOND A): IF We are on a Stay Task currently, we should have changed is already in the first step above

                // SECOND B): If We are on a Drive Task currently, we have to see if the planed direction still fits our needs
                if (currentTask.getTaskType().equals(AmodeusTaskType.DRIVE)) {
                    GlobalAssert.that(isSecondLastTask);
                    Link planedToLink = ((AmodeusDriveTask) currentTask).getPath().getToLink();
                    if (!planedToLink.equals(currentCourse.get().getLink()))
                        if (!planedToLink.equals(roboTaxi.getDivertableLocation()))
                            divert = true;
                        else
                            // TODO @clruch 20190901 remove soon if no errors
                            GlobalAssert.that(roboTaxi.getStatus().equals(SharedRoboTaxiUtils.calculateStatusFromMenu(roboTaxi)));
                    else
                        // TODO @clruch 20190901 remove soon if no errors
                        GlobalAssert.that(roboTaxi.getStatus().equals(SharedRoboTaxiUtils.calculateStatusFromMenu(roboTaxi)));
                    if (planedToLink.equals(roboTaxi.getDivertableLocation()) && isOnLastTask)
                        GlobalAssert.that(divert); // should be set to true as it is second last task and and finished
                }

                // SECOND C): If We are on a Pickup or Dropoff Task currently, we should wait until we reach the end of this task as we are not going to abort.
                // But then The
                // First part helps us.

                // THIRD AND FINAL: Divert If Required
                if (divert)
                    return Optional.of(currentCourse.get().getLink());
            }
        } else {
            // HERE WE MAKE SURE THE STATUS IS SET CORRECT AFTER THE FINISH OF THE LAST TASK
            // FIRST a): if there is no curse and we are on the last task then All is fine as long as we are in Stay Status
            if (isOnLastTask) {
                GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.STAY));
            } else if (isSecondLastTask) {
                if (taskEndsNow) { // FIRST b): if we will finish the second last task now then the next status will be stay. As we have nothing to do.
                    switch ((AmodeusTaskType) currentTask.getTaskType()) {
                    case DRIVE:
                        // AS there is no task after this one and the currend destinadtion is not the current location we have to divert the robo taxi to the
                        // current location
                        if (!roboTaxi.getDivertableLocation().equals(((AmodeusDriveTask) currentTask).getPath().getToLink())) {
                            SharedCourse redirectCourse = SharedCourse.redirectCourse(roboTaxi.getDivertableLocation(),
                                    Double.toString(now) + "_currentLink_" + roboTaxi.getId().toString());
                            roboTaxi.addRedirectCourseToMenuAtBegining(redirectCourse);
                            return Optional.of(roboTaxi.getDivertableLocation());
                        }
                    case DROPOFF:
                        // TODO @clruch 20190901 remove soon if no errors
                        GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.STAY));
                        break;
                    default:
                        throw new RuntimeException("no can do: " + currentTask.getTaskType());
                    }
                } else { // SECOND: if we are on the second last task but it does not end yet then we have to stop the current task if that's possible
                    // Lets consider the Case were we are in a drive Task
                    if (currentTask.getTaskType() == AmodeusTaskType.DRIVE) {
                        AmodeusDriveTask driveTask = (AmodeusDriveTask) currentTask;
                        // AS there is no task after this one and the currend destinadtion is not the current location we have to divert the robo taxi to the
                        // current location
                        if (!roboTaxi.getDivertableLocation().equals(driveTask.getPath().getToLink())) {
                            SharedCourse redirectCourse = SharedCourse.redirectCourse(roboTaxi.getDivertableLocation(),
                                    Double.toString(now) + "_currentLink_" + roboTaxi.getId().toString());
                            roboTaxi.addRedirectCourseToMenuAtBegining(redirectCourse);
                            return Optional.of(roboTaxi.getDivertableLocation());
                        }
                        // TODO @clruch 20190901 remove soon if no errors
                        GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.STAY));
                    } else {
                        // We only do it for Drive Tasks. As:
                        // a) A dropoff Task already finishes by default with a stay task afterwards. Thus The only reason we reach this part is because we are
                        // in
                        // Dropoff
                        GlobalAssert.that(currentTask.getTaskType() == AmodeusTaskType.DROPOFF);
                        GlobalAssert.that(roboTaxi.getStatus().equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
                        // b) A stay task should never be the second last Task.
                        // c) A pickup Task always needs to have a next course in the menu. but that we can check as well
                    }
                }
            } else {
                System.out.println("Thats a case is not allowed. It means that we plan more than two tasks ahead");
                System.out.println("This is only allowed after the redispatchInternal() until the end of the Time Step");
                throw new RuntimeException("no can do");
            }
        }
        return Optional.empty();
    }
}
