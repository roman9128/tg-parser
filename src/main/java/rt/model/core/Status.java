package rt.model.core;

import java.util.concurrent.atomic.AtomicBoolean;

public class Status {
    private static AtomicBoolean isReadyToInteract = new AtomicBoolean(false);

    public static boolean isReadyToInteract() {
        return isReadyToInteract.get();
    }

    public static void setReadyToInteract(boolean isReadyToInteract) {
        Status.isReadyToInteract.set(isReadyToInteract);
    }
}
