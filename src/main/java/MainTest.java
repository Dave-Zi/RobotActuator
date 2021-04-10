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
import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class MainTest {

    public static void main(String[] args) throws IOException, TimeoutException {

//        HashMap<BoardTypeEnum, List<IBoard>> boards = Robot.JsonToRobot("{\"EV3\":[{\"Port\":\"rfcomm3\"}],\"GrovePi\":[{\"A0\":\"\",\"A1\":\"\",\"A2\":\"\",\"D2\":\"Led\",\"D3\":\"\",\"D4\":\"Ultrasonic\",\"D5\":\"\",\"D6\":\"\",\"D7\":\"\",\"D8\":\"Led\"}]}}");

//        Ev3Board ev3B = (Ev3Board) boards.get(BoardTypeEnum.EV3).get(0);
//        GrovePiBoard grovePi = (GrovePiBoard) boards.get(BoardTypeEnum.GrovePi).get(0);

        RobotSensorsData robotSensorsData = new RobotSensorsData();
        ConnectionHandler connectionHandler = new ConnectionHandler(robotSensorsData);
        connectionHandler.openQueues();

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
