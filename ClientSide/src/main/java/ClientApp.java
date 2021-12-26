import java.io.IOException;

public class ClientApp {
    public static void main(String[] args) {
        try {
            new EchoClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
