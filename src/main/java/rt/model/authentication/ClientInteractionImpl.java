package rt.model.authentication;

import it.tdlight.client.*;
import it.tdlight.util.ScannerUtils;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class ClientInteractionImpl implements ClientInteraction {
    private final ExecutorService blockingExecutor;
    private final Authenticable authenticable;

    public ClientInteractionImpl(ExecutorService blockingExecutor, Authenticable authenticable) {
        this.blockingExecutor = blockingExecutor;
        this.authenticable = authenticable;
    }

    @Override
    public CompletableFuture<String> onParameterRequest(InputParameter parameter, ParameterInfo parameterInfo) {
        var authSupplier = this.authenticable.getAuthenticationSupplier();
        AuthenticationData authData = this.getAuthDataNowOrNull(authSupplier);
        return CompletableFuture.supplyAsync(() -> {
            boolean useRealWho = authData != null;
            String who;
            if (!useRealWho) {
                who = "new user";
            } else {
                who = "+" + authData.getUserPhoneNumber();
            }
            boolean trim = false;
            String question;
            switch (parameter) {
                case ASK_CODE:
                    question = "Enter authentication code";
                    ParameterInfoCode codeInfo = (ParameterInfoCode)parameterInfo;
                    question = question + "\n\tCode type: " + codeInfo.getType().getClass().getSimpleName().replace("AuthenticationCodeType", "");
                    if (codeInfo.getNextType() != null) {
                        question = question + "\n\tNext code type: " + codeInfo.getNextType().getClass().getSimpleName().replace("AuthenticationCodeType", "");
                    }
                    trim = true;
                    break;
                case ASK_PASSWORD:
                    question = "Enter your password";
                    break;
                default:
                    question = parameter.toString();
            }
            String result = ScannerUtils.askParameter(who, question);
            return trim ? result.trim() : Objects.requireNonNull(result);
        }, this.blockingExecutor);
    }

    private AuthenticationData getAuthDataNowOrNull(AuthenticationSupplier<?> authSupplier) {
        try {
            return authSupplier.get().getNow(null);
        } catch (Throwable thr) {
            return null;
        }
    }
}