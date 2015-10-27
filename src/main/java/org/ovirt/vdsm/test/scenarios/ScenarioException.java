package org.ovirt.vdsm.test.scenarios;

public class ScenarioException extends Exception {

    private static final long serialVersionUID = -2008833488730036957L;

    public ScenarioException() {
    }

    public ScenarioException(String message) {
        super(message);
    }

    public ScenarioException(Throwable cause) {
        super(cause);
    }

    public ScenarioException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScenarioException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
