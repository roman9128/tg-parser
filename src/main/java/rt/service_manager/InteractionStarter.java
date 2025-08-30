package rt.service_manager;

public interface InteractionStarter {
    void startInteractions();
    void showQrCode(String link);
    String ask2FAPassword();
}
