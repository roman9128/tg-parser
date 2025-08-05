package rt.infrastructure.parser;

import it.tdlight.client.*;
import rt.service_manager.ParameterRequester;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class ClientInteractionImpl implements ClientInteraction {
    private final ExecutorService blockingExecutor;
    private final PhoneAuthentication phoneAuthentication;
    private final ParameterRequester parameterRequester;

    public ClientInteractionImpl(ExecutorService blockingExecutor, PhoneAuthentication phoneAuthentication, ParameterRequester parameterRequester) {
        this.blockingExecutor = blockingExecutor;
        this.phoneAuthentication = phoneAuthentication;
        this.parameterRequester = parameterRequester;
    }

    @Override
    public CompletableFuture<String> onParameterRequest(InputParameter parameter, ParameterInfo parameterInfo) {
        return CompletableFuture.supplyAsync(() -> {
            boolean useRealWho = phoneAuthentication != null;
            String who;
            if (!useRealWho) {
                who = "Новый пользователь";
            } else {
                who = "+" + phoneAuthentication.get().getNow(null).getUserPhoneNumber();
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
}