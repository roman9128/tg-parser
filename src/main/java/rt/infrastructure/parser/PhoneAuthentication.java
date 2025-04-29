package rt.infrastructure.parser;

import it.tdlight.client.AuthenticationData;
import it.tdlight.client.AuthenticationSupplier;
import rt.model.service.ParameterRequester;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class PhoneAuthentication implements AuthenticationSupplier<AuthenticationData> {

    private static final class State implements AuthenticationData {
        final boolean isQr = false;
        final boolean isBot = false;
        final String botToken = null;
        final String phoneNumber;

        private State(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        @Override
        public boolean isQrCode() {
            return isQr;
        }

        @Override
        public boolean isBot() {
            return isBot;
        }

        @Override
        public String getUserPhoneNumber() {
            return phoneNumber;
        }

        @Override
        public String getBotToken() {
            throw new UnsupportedOperationException("This is not a bot");
        }
    }

    private final AtomicReference<CompletableFuture<AuthenticationData>> state = new AtomicReference<>();
    private final ParameterRequester parameterRequester;

    public PhoneAuthentication(ParameterRequester parameterRequester) {
        this.parameterRequester = parameterRequester;
    }

    public CompletableFuture<AuthenticationData> askData() {
        return get();
    }

    public boolean isInitialized() {
        CompletableFuture<AuthenticationData> cf = state.get();
        return cf != null && cf.isDone();
    }

    @Override
    public CompletableFuture<AuthenticationData> get() {
        CompletableFuture<AuthenticationData> cf = new CompletableFuture<>();
        if (state.compareAndSet(null, cf)) {
            SequentialRequestsExecutor.getInstance().execute(() -> {
                try {
                    String phoneNumber;
                    do {
                        phoneNumber = parameterRequester.askParameter("new user", "enter your phone number");
                    } while (phoneNumber.length() < 3);
                    cf.complete(new State(phoneNumber));
                } catch (Throwable ex) {
                    cf.completeExceptionally(ex);
                    throw ex;
                }
            });
            return cf;
        } else {
            return state.get();
        }
    }
}