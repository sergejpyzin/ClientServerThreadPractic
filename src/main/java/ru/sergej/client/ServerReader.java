package ru.sergej.client;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

class ServerReader implements Runnable {
    private final Socket serverSocket;

    public ServerReader(Socket serverSocket) {
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try (Scanner in = new Scanner(serverSocket.getInputStream())) {
            while (in.hasNext()) {
                String input = in.nextLine();
                if (input.equals("/exit") || input.equals("/all")){
                    continue;
                }
                System.out.println("Сообщение от сервера: " + input);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при отключении чтении с сервера: " + e.getMessage());
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            System.err.println("Ошибка при отключении от сервера: " + e.getMessage());
        }
    }

}
