package ru.sergej.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Collectors;

class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final PrintWriter out;
    private final Map<String, ClientHandler> clients;
    private final String clientId;

    public ClientHandler(Socket clientSocket, Map<String, ClientHandler> clients, String clientId) throws IOException {
        this.clientSocket = clientSocket;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.clients = clients;
        this.clientId = clientId;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    @Override
    public void run() {
        try (Scanner in = new Scanner(clientSocket.getInputStream())) {
            while (true) {
                String input = in.nextLine();
                System.out.println("Получено сообщение от клиента " + clientSocket + ": " + input);

                String toClientId = null;
                if (input.startsWith("@")) {
                    String[] parts = input.split("\\s+");
                    if (parts.length > 0) {
                        toClientId = parts[0].substring(1);
                    }
                }

                if (toClientId == null) {
                    clients.values().forEach(it -> it.send(input));
                } else {
                    ClientHandler toClient = clients.get(toClientId);
                    if (toClient != null) {
                        toClient.send(input.replace("@" + toClientId + " ", ""));
                    } else {
                        System.err.println("Не найден клиент с идентификатором: " + toClientId);
                    }
                }

                if (Objects.equals("/all", input)) {
                    // Отправляем список всех текущих пользователей клиенту
                    String allClientsList = clients.keySet().stream()
                            .collect(Collectors.joining("\n"));
                    out.println("Список всех текущих пользователей:\n" + allClientsList);
                }

                if (Objects.equals("/exit", input)) {
                    System.out.println("Клиент отключился");
                    // Удаляем клиента из списка активных клиентов
                    clients.remove(clientId);
                    // Оповещаем других клиентов об отключении
                    for (ClientHandler otherClient : clients.values()) {
                        if (!otherClient.getClientSocket().equals(clientSocket)) {
                            otherClient.send("Клиент " + clientId + " отключился");
                        }
                    }
                    break;
                } else if (Objects.equals(("/all"), input)) {


                } else {
                    out.println("Cообщение [" + input + "] получено");

                }
            }
        } catch (IOException e) {
            System.err.println("Произошла ошибка при взаимодействии с клиентом " + clientSocket + ": " + e.getMessage());
        }

        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Ошибка при отключении клиента " + clientSocket + ": " + e.getMessage());
        }
    }


    public void send(String msg) {
        out.println(msg);
    }

}
