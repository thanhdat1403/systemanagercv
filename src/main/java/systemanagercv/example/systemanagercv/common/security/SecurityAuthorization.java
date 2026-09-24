package systemanagercv.example.systemanagercv.common.security;

public final class SecurityAuthorization {

    private SecurityAuthorization() {
        // ngăn chặn việc tự khởi tạo
    }

    // =====================================================
    // GENERAL
    // =====================================================

    public static final String ADMIN =
            "hasRole('ADMIN')";

    public static final String ADMIN_OR_HR =
            "hasAnyRole('ADMIN', 'HR')";


    // =====================================================
    // EMPLOYEE
    // =====================================================

    public static final String EMPLOYEE_READER =
            "hasAnyRole('ADMIN', 'HR', 'TECH_LEAD', 'EMPLOYEE')";

    public static final String EMPLOYEE_CREATOR =
            "hasAnyRole('ADMIN', 'HR', 'TECH_LEAD')";

    public static final String EMPLOYEE_EDITOR =
            "hasAnyRole('ADMIN', 'HR', 'TECH_LEAD', 'EMPLOYEE')";

    public static final String EMPLOYEE_DELETER =
            "hasAnyRole('ADMIN', 'HR', 'TECH_LEAD')";


    // =====================================================
    // CV
    // =====================================================

    public static final String CV_READER =
            "hasAnyRole('ADMIN', 'HR', 'TECH_LEAD', 'EMPLOYEE')";

    public static final String CV_CREATOR =
            "hasAnyRole('ADMIN', 'HR', 'TECH_LEAD', 'EMPLOYEE')";

    public static final String CV_EDITOR =
            "hasAnyRole('ADMIN', 'HR', 'TECH_LEAD', 'EMPLOYEE')";

    public static final String CV_DELETER =
            "hasAnyRole('ADMIN', 'HR')";

    public static final String CV_SUBMITTER =
            "hasAnyRole('TECH_LEAD', 'EMPLOYEE')";

    public static final String TECH_LEAD_CV_REVIEWER =
            "hasRole('TECH_LEAD')";

    public static final String HR_CV_REVIEWER =
            "hasAnyRole('ADMIN', 'HR')";

}