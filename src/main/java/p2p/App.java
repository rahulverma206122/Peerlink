package p2p;

import p2p.controller.FileController;
import java.io.IOException;

/**
 * PeerLink - P2P File Sharing Application
 */
public class App {
    public static void main(String[] args) {
        try {
            // Use Render's dynamic PORT or fallback to 8080 for local testing
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

            // Start the API server on the chosen port
            FileController fileController = new FileController(port);
            fileController.start();

            System.out.println("PeerLink server started on port " + port);
            System.out.println("UI available at http://localhost:3000");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                fileController.stop();
            }));

            // Keep server running (instead of waiting for Enter key)
            Thread.currentThread().join();

        } catch (IOException | InterruptedException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
