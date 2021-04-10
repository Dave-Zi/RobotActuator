import Enums.BoardTypeEnum;
import Enums.Ev3DrivePort;
import Enums.IEv3Port;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Delivery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

class ConnectionHandler {

    private Channel sendChannel, receiveChannel;
    private final String RECEIVE_QUEUE_NAME = "Commands";
    private final String SEND_QUEUE_NAME = "Data";
    private HashMap<BoardTypeEnum, List<IBoard>> boards;

    private ConnectionFactory factory;
    private RobotSensorsData robotSensorsData;

    ConnectionHandler(RobotSensorsData robotSensorsData) {
        this.robotSensorsData = robotSensorsData;
//        openQueues();
    }

    void openQueues() throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        Connection connection = factory.newConnection();

        sendChannel = connection.createChannel();
        sendChannel.queueDeclare(SEND_QUEUE_NAME, false, false, false, null);


        receiveChannel = connection.createChannel();
        receiveChannel.queueDeclare(RECEIVE_QUEUE_NAME, false, false, false, null);

        receiveChannel.basicConsume(RECEIVE_QUEUE_NAME, true, this::onReceiveCallback, consumerTag -> { });
    }

    private void send(String message) throws IOException {
        sendChannel.basicPublish("", SEND_QUEUE_NAME, null, message.getBytes());
    }

    private void onReceiveCallback(String consumerTag, Delivery delivery) throws IOException {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        System.out.println(" [x] Received '" + message + "'");

        JsonObject obj = new JsonParser().parse(message).getAsJsonObject();
        String command = String.valueOf(obj.get("Command"));
        String dataJsonString = String.valueOf(obj.get("Data"));
        switch (command){
            case "\"Subscribe\"":
                System.out.println("in subscribe!");

                robotSensorsData.addToBoardsMap(dataJsonString);
                break;

            case "\"Unsubscribe\"":
                System.out.println("in unsubscribe!");
                robotSensorsData.removeFromBoardsMap(dataJsonString);
                break;

            case "\"Build\"":
                boards = Robot.JsonToRobot(dataJsonString);
                System.out.println("building success!");
                break;

            case "\"Drive\"":
                Map<String, Map<String, Map<String, Double>>> ev3Board = robotSensorsData.jsonToBoardsMap(dataJsonString);
                Map<String, Map<String, Double>> ev3s = ev3Board.get("EV3");
                for(Map.Entry<String, Map<String, Double>> ev3: ev3s.entrySet()){
                    Ev3Board ev3B = (Ev3Board) boards.get(BoardTypeEnum.EV3).get(Integer.parseInt(ev3.getKey()));
                    Map<IEv3Port, Double> forward = new HashMap<>();
                    for(Map.Entry<String, Double> motorAndSpeed: ev3.getValue().entrySet()){
                        IEv3Port port = Ev3DrivePort.valueOf(motorAndSpeed.getKey());
                        Double speed = motorAndSpeed.getValue();
                        forward.put(port, speed);
                        robotSensorsData.setPortValue("EV3", ev3.getKey(), motorAndSpeed.getKey(), speed);
                    }
                    ev3B.drive(forward);
                }
                break;

            default:
                break;
        }
    }

    HashMap<BoardTypeEnum, List<IBoard>> getBoards() {
        return boards;
    }

    // =========================== TEST ===============================

    private void sendTest() throws IOException {

        while (true){
            send(robotSensorsData.toJson());
//            String jsonDataString = String.format("{\"EV3\": {\"_1\": {\"_2\": %s}, \"_2\": {\"_2\": 20, \"_3\": 20}, \"_3\": {\"_2\": 20}}, GrovePi: {}}", x); // Example
//            sendChannel.basicPublish("", SEND_QUEUE_NAME, null, jsonDataString.getBytes());

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}


