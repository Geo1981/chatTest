package Services.interfaces;

public interface AuthenticationServices {
    void start();
    void stop();
    String getNickNameByLoginAndPassword(String login, String password);
}
