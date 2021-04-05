import Enums.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class MainTest {

    private final static String QUEUE_NAME = "Cafe";

    public static void main(String[] args) throws IOException, TimeoutException {

        HashMap<BoardTypeEnum, List<IBoard>> boards = Robot.JsonToRobot(args[0]);
        Ev3Board ev3B = (Ev3Board) boards.get(BoardTypeEnum.EV3).get(0);
        GrovePiBoard grovePi = (GrovePiBoard) boards.get(BoardTypeEnum.GrovePi).get(0);


        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {

            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");

            JsonObject obj = new JsonParser().parse(message).getAsJsonObject();
            String command = String.valueOf(obj.get("Command"));
            String dataJsonString = String.valueOf(obj.get("Data"));
            switch (command){
                case "\"Drive\"":
                    JsonObject dataJsonObj = new JsonParser().parse(dataJsonString).getAsJsonObject();
                    String driveDataJsonString = String.valueOf(dataJsonObj.get("EV3"));
                    JsonObject driveDataJsonObj = new JsonParser().parse(driveDataJsonString).getAsJsonObject();
                    Map<IEv3Port, Double> forward = new HashMap<>();
                    for(Map.Entry<String, JsonElement> entry: driveDataJsonObj.entrySet()){
                        IEv3Port port = Ev3DrivePort.valueOf(entry.getKey());
                        double speed = entry.getValue().getAsDouble();
                        forward.put(port, speed);
                        System.out.println("port: "+ port +", speed:"+ speed);
                    }
                    ev3B.drive(forward);
                    break;

                default:
                    break;
            }
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });

//        Map<IEv3Port, Double> forward2 = Map.of(
//                Ev3DrivePort.B, 35.0,
//                Ev3DrivePort.C, 35.0
//        );
//        ev3B.drive(forward2);

//        Map<IEv3Port, Double> stop = Map.of();
//        Map<IEv3Port, Double> forward = Map.of(
//                Ev3DrivePort.B, 35.0,
//                Ev3DrivePort.C, 35.0
//        );
//
//        Map<IEv3Port, Double> turn = Map.of(
//                Ev3DrivePort.B, 25.0
//        );
//
//
//        int count = 0;
//        while (count < 2) {
//            ev3B.drive(forward);
//            grovePi.setSensorData(GrovePiPort.D2, true);
//            grovePi.setSensorData(GrovePiPort.D8, false);
//
//            Double ev3Distance = ev3B.getDoubleSensorData(Ev3SensorPort._2, 0);
//            double groveDistance = grovePi.getDoubleSensorData(GrovePiPort.D4, 0);
//
//            System.out.println("Driving straight!");
//            while (ev3Distance == null || (ev3Distance > 30 && groveDistance > 20)) {
//                ev3Distance = ev3B.getDoubleSensorData(Ev3SensorPort._2, 0);
//                groveDistance = grovePi.getDoubleSensorData(GrovePiPort.D4, 0);
//            }
//            System.out.println("Stopping");
//
//            ev3B.drive(stop);
//            grovePi.setSensorData(GrovePiPort.D2, false);
//            grovePi.setSensorData(GrovePiPort.D8, true);
//
//            while (groveDistance > 20) {
//                groveDistance = grovePi.getDoubleSensorData(GrovePiPort.D4, 0);
//            }
//            System.out.println("Turning!");
//
//
//            ev3B.drive(turn);
//            Thread.sleep(3000);
//            ev3B.drive(stop);
//            grovePi.setSensorData(GrovePiPort.D8, false);
//
//            count++;
//        }
//        ev3B.drive(stop);
//        grovePi.setSensorData(GrovePiPort.D2, false);
//        grovePi.setSensorData(GrovePiPort.D8, false);
//        System.out.println("End!");
    }

}
