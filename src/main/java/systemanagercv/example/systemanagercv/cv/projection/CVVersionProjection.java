package systemanagercv.example.systemanagercv.cv.projection;

import systemanagercv.example.systemanagercv.cv.enums.CvVersionStatus;

public interface CVVersionProjection {

    Long getId();

    Long getEmployeeCvId();

    String getVersion();

    CvVersionStatus getStatus();

    Boolean getCurrent();
}