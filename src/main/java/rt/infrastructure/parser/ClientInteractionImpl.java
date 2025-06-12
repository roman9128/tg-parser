package rt.infrastructure.parser;

import it.tdlight.client.*;
import rt.model.service.ParameterRequester;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

class ClientInteractionImpl implements ClientInteraction {
    private final ExecutorService blockingExecutor;
    private final Authenticable authenticable;
    private final ParameterRequester parameterRequester;

    ClientInteractionImpl(ExecutorService blockingExecutor, Authenticable authenticable, ParameterRequester parameterRequester) {
        this.blockingExecutor = blockingExecutor;
        this.authenticable = authenticable;
        this.parameterRequester = parameterRequester;
    }

    @Override
    public CompletableFuture<String> onParameterRequest(InputParameter parameter, ParameterInfo parameterInfo) {
        var authSupplier = this.authenticable.getAuthenticationSupplier();
        AuthenticationData authData = this.getAuthDataNowOrNull(authSupplier);
        return CompletableFuture.supplyAsync(() -> {
            boolean useRealWho = authData != null;
            String who;
            if (!useRealWho) {
                who = "Новый пользователь";
            } else {
                who = "+" + authData.getUserPhoneNumber();
            }
            boolean trim = false;
            String question;
            switch (parameter) {
                case ASK_CODE:
                    question = "Введите код подтверждения";
                    ParameterInfoCode codeInfo = (ParameterInfoCode) parameterInfo;
                    question = question + System.lineSeparator() + "Тип кода: " + codeInfo.getType().getClass().getSimpleName().replace("AuthenticationCodeType", "");
                    if (codeInfo.getNextType() != null) {
                        question = question + System.lineSeparator() + "Следующий тип кода: " + codeInfo.getNextType().getClass().getSimpleName().replace("AuthenticationCodeType", "");
                    }
                    trim = true;
                    break;
                case ASK_PASSWORD:
                    question = "Введите пароль";
                    break;
                default:
                    question = parameter.toString();
            }
            String result = parameterRequester.askParameter(who, question);
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