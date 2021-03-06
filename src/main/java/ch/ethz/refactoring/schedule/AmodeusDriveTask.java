package ch.ethz.refactoring.schedule;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.matsim.contrib.dvrp.path.VrpPathWithTravelData;
import org.matsim.contrib.dvrp.schedule.DriveTask;

import ch.ethz.matsim.av.passenger.AVRequest;

public class AmodeusDriveTask extends DriveTask {
    private final Set<AVRequest> requests = new HashSet<>();

    public AmodeusDriveTask(VrpPathWithTravelData path) {
        super(AmodeusTaskType.DRIVE, path);
    }

    public AmodeusDriveTask(VrpPathWithTravelData path, Collection<AVRequest> requests) {
        this(path);
        this.requests.addAll(requests);
    }
}
