package p2p.service;

import p2p.utils.UploadUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class FileSharer {

    private HashMap<Integer, String> availableFiles;  // availableFiles stores files you want to share, using an integer port code as the key and the file path as the value.

    public FileSharer() {   // This is called a constructor. A constructor is a special method that runs automatically when you create an object of this class.
        availableFiles = new HashMap<>();  // This creates a new empty HashMap.
    }

    public int offerFile(String filePath) {   // This method allows you to share a file. It takes the file path as an argument and returns a unique port code. public → anyone can call this method from outside the class.filePath → the full path of the file you want to share, like "C:/file.txt".
        int port;
        while (true) {  // This loop keeps trying to generate a unique port code until it finds one that isn't already used.
            port = UploadUtils.generateCode();  // generates a random number (like 1234, 5678, etc.). generateCode() is a method in another class called UploadUtils. uploadutils me genertate code nam ka method bna hoga jiske andar hmne math.random use krke sirf 4 digit ka code generate kiya h
            if (!availableFiles.containsKey(port)) {  // containsKey() is a predefined method of Java’s HashMap class.Checks if a specific key exists in the HashMap.
                availableFiles.put(port, filePath);
                return port;
            }
        }
    }

    public void startFileServer(int port) {
        String filePath = availableFiles.get(port);  //looks up which file is stored with this code/port.
        if (filePath == null) {
            System.err.println("No file associated with port: " + port);
            return;
        }
 // the messages from System.out.println will appear in VS Code’s terminal, not the browser console.System.err.println also prints to the VS Code terminal
 //Browser console: still won’t show anything from System.err.println because that’s Java, not JavaScript.
        try (ServerSocket serverSocket = new ServerSocket(port)) {  // ServerSocket is like a door waiting for someone to connect. it is java class , try(...) → automatically closes the socket when done. 
            System.out.println("Serving file '" + new File(filePath).getName() + "' on port " + port); // new File(filePath).getName() → gets just the name of the file from the full path. Tells us: “Server is ready and waiting for a client to download the file.”
            Socket clientSocket = serverSocket.accept();// accept() waits here until a client connects. When a client connects, it returns a Socket object representing that connection.
            System.out.println("Client connected: " + clientSocket.getInetAddress());  // clientSocket.getInetAddress() → prints the client’s IP address.

            new Thread(new FileSenderHandler(clientSocket, filePath)).start();  // Creates a new thread to handle sending the file to the client. This allows the server to handle multiple clients at once. (a mini-program running at the same time). .start() → runs the thread.
            // FileSenderHandler is a class defined below that takes care of reading the file and sending it over the network.
            // Without a thread, your server can only send one file at a time.
            // With a thread, multiple clients can download files at the same time.
        } catch (IOException e) {
            System.err.println("Error starting file server on port " + port + ": " + e.getMessage());
        }
    }

    private static class FileSenderHandler implements Runnable {  // FileSenderHandler is responsible for actually sending the file to the client once they connect.
        private final Socket clientSocket; // clientSocket → the connection to the client (so we know where to send the file). is line ka matlab h ki ye variable sirf is class ke andar hi use hoga. final → means these variables cannot change once assigned.
        private final String filePath;  // final → means these variables cannot change once assigned.

        public FileSenderHandler(Socket clientSocket, String filePath) {
            this.clientSocket = clientSocket;
            this.filePath = filePath;
        }

        @Override  // Tells the compiler: “we’re overriding a method from a parent class or interface.”
        public void run() { // Here it indicates we are implementing run() from the Runnable interface.run() is the entry point for the thread. When you call new Thread(new FileSenderHandler(...)).start(), Java calls this run() method inside the new thread.
            try (FileInputStream fis = new FileInputStream(filePath);  // FileInputStream → reads the file from your computer. try(...) → automatically closes the file and socket when done. If file not found → FileNotFoundException (a subclass of IOException) is thrown.
                 OutputStream oss = clientSocket.getOutputStream()) {  // clientSocket.getOutputStream() returns an OutputStream connected to the client over the network. Important detail: closing the socket's OutputStream may half-close or close the socket connection depending on implementation; you still explicitly close the socket later in finally for safety.
                
                // Send the filename as a header
                String filename = new File(filePath).getName();
                String header = "Filename: " + filename + "\n";  //Example: "Filename: song.mp3\n"
                oss.write(header.getBytes()); // Send the header to the client. getBytes() converts the string to bytes for sending over the network.
                

                // can i send a file whose size is around 10 mb
                // yes bcz You are not loading the whole file into memory at once. we use the loop to read and send it in small chunks (4096 bytes at a time). This is memory efficient and works well even for large files.
                // fis.read(buffer) → reads up to 4096 bytes (4 KB) at a time. The loop repeats until the entire file (10 MB) is transferred.

                // can i send file of 2 gb
                // yes but it depends on the network speed and stability. Sending a 2 GB file over the internet can take a long time (potentially hours on a slow connection) and is more prone to interruptions. However, the code itself can handle large files because it reads and sends the file in small chunks (4096 bytes at a time) without loading the entire file into memory.
                // For huge transfers, you might need to increase socket timeout so the connection doesn’t close if it’s slow.

                // Send the file content
                byte[] buffer = new byte[4096]; // byte[] This means we are creating an array of bytes.new byte[4096]- This creates a new byte array with 4096 slots.That means the buffer can temporarily hold 4096 bytes (≈ 4 KB) of data at a time. 4096 is common and fine. You might use 8192 (8 KB)
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) { // fis.read(buffer) reads up to buffer.length bytes and returns how many bytes were actually read, or -1 if EOF (end-of-file).
                    oss.write(buffer, 0, bytesRead); // sends those bytes to the client.  why use buffer-> Sending data in chunks (like 4096 bytes at a time) is efficient and works for large files (no need to load entire file into memory).
                }
                System.out.println("File '" + filename + "' sent to " + clientSocket.getInetAddress()); // Prints success status to the console (VS Code terminal), helpful for debugging.
            } catch (IOException e) {
                System.err.println("Error sending file to client: " + e.getMessage());
            } finally {  // finally block always runs, whether an exception occurred or not. It’s used for cleanup.
                try { // Closing the socket inside finally prevents resource leaks.
                    clientSocket.close();  // clientSocket.close() releases network resources and signals EOF to the client. always do this
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
}



// Runnable is a built-in interface in Java.It lives in the package java.lang (so you don’t even need to import it).It has only one method inside it:
// public interface Runnable {
//public abstract void run();
// }
//So yes — run() is already defined in Runnable (but only as a method signature, no code).
// public void run();  // run() is where you put the code that should execute when the thread starts. When you create a new Thread and call start(), Java calls the run() method of the Runnable you passed in.

// Why we override run()
// when we write this 
// private static class FileSenderHandler implements Runnable {
//     @Override
//     public void run() {
//         // your custom code
//     }
// }
// You are saying:
// “Hey Java, when this class is used as a thread, this is the code (run()) I want it to execute.”
// Without overriding run(), the thread would have nothing to do.

// FileSharer → Keeps track of files available for sharing.

// FileSenderHandler → Runs in a thread, actually reads the file from disk and sends it to the client.

// Runnable with run() → Allows multiple transfers at once (parallelism).

// Real-Life Analogy

// Imagine you’re a teacher with lots of notes (files).

// Students (clients) ask you for notes.

// If you only help one student at a time (no threads), the others wait forever.

// With threads, you can give notes to all students at the same time.


