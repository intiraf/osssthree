/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package five;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class Server {
    public static void main(String[] args) throws IOException {
        // ขอ IP จากผู้ใช้
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Server IP: ");
        String serverIp = scanner.nextLine();
        InetSocketAddress serverAddress = new InetSocketAddress(serverIp, 8888);

        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(serverAddress);
            System.out.println("Server is listening on " + serverIp + ":8888");

            while (true) {
                try (SocketChannel socketChannel = serverSocketChannel.accept()) {
                    Path filePath = Path.of("D:/os/receive/file.txt");
                    FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ);

                    // Zero Copy: Transfer file directly from file channel to socket channel
                    long zeroCopyStartTime = System.nanoTime();
                    fileChannel.transferTo(0, fileChannel.size(), socketChannel);
                    long zeroCopyEndTime = System.nanoTime();
                    System.out.println("Zero Copy Time: " + (zeroCopyEndTime - zeroCopyStartTime) + " ns");

                    // Classic Copy: Use InputStream and OutputStream
                    long classicCopyStartTime = System.nanoTime();
                    try (InputStream inputStream = Files.newInputStream(filePath);
                         OutputStream outputStream = socketChannel.socket().getOutputStream()) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    long classicCopyEndTime = System.nanoTime();
                    System.out.println("Classic Copy Time: " + (classicCopyEndTime - classicCopyStartTime) + " ns");
                }
            }
        }
    }
}

