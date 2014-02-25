package com.giorgioaresu.batchrenamer;

import eu.chainfire.libsuperuser.Shell;

public class SuHelper {
    public static final int SU_UNKNOWN = 0;
    public static final int SU_AVAILABLE = 1;
    public static final int SU_NOT_AVAILABLE = 2;
    private static int suStatus = SU_UNKNOWN;

    /**
     * Check if SU is available. After first check, the value is stored to avoid unnecessary calls
     *
     * @return
     */
    public static boolean isSuAvailable() {
        if (suStatus == SU_UNKNOWN) {
            boolean suAvailable = Shell.SU.available();
            suStatus = suAvailable ? SU_AVAILABLE : SU_NOT_AVAILABLE;
        }
        return suStatus == SU_AVAILABLE;
    }

    public static void resetSuStatus() {
        suStatus = SU_UNKNOWN;
    }
}
