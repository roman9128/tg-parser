package rt.infrastructure.parser;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class SequentialRequestsExecutor implements Executor {

    private static volatile SequentialRequestsExecutor INSTANCE;

    private final Executor executor = Executors.newSingleThreadExecutor(r -> {
        final Thread thread = new Thread(r);
        thread.setName("TDLight user input request");
        thread.setDaemon(true);
        return thread;
    });

    private SequentialRequestsExecutor() {
    }

    static SequentialRequestsExecutor getInstance() {
        SequentialRequestsExecutor instance = INSTANCE;
        if (instance == null) {
            synchronized (SequentialRequestsExecutor.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SequentialRequestsExecutor();
                }
                instance = INSTANCE;
            }
        }
        return instance;
    }

    @Override
    public void execute(Runnable command) {
        executor.execute(command);
    }
}
