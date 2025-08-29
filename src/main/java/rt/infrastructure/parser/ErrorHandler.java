package rt.infrastructure.parser;

import it.tdlight.client.GenericResultHandler;
import it.tdlight.client.Result;
import it.tdlight.jni.TdApi;
import rt.service_manager.ErrorInformer;

class ErrorHandler implements GenericResultHandler<TdApi.Ok> {

    private final ErrorInformer errorInformer;

    ErrorHandler(ErrorInformer errorInformer) {
        this.errorInformer = errorInformer;
    }

    @Override
    public void onResult(Result<TdApi.Ok> result) {
        if (result.getError() != null) {
            errorInformer.informAboutError("Произошла ошибка при авторизации: " + result.getError() + System.lineSeparator() + "Требуется перезапуск");
            System.exit(0);
        }
    }
}