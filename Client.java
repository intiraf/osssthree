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
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException {
        // ขอ IP จากผู้ใช้
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Server IP: ");
        String serverIp = scanner.nextLine();
        InetSocketAddress serverAddress = new InetSocketAddress(serverIp, 8888);

        try (SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(serverAddress);

            Path filePath = Path.of("/home/oss/Downloads/receive/yourfile.txt");
            FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

            // Zero Copy: Transfer file directly from socket channel to file channel
            long zeroCopyStartTime = System.nanoTime();
            fileChannel.transferFrom(socketChannel, 0, Long.MAX_VALUE);
            long zeroCopyEndTime = System.nanoTime();
            System.out.println("Zero Copy Time: " + (zeroCopyEndTime - zeroCopyStartTime) + " ns");

            // Classic Copy: Use InputStream and OutputStream
            long classicCopyStartTime = System.nanoTime();
            try (InputStream inputStream = socketChannel.socket().getInputStream();
                 OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
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

