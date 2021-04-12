import Communication.CommunicationHandler;
import RobotData.RobotSensorsData;
import com.rabbitmq.client.Delivery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class MainTest {

    private static CommunicationHandler communicationHandler;
    private static CommandHandler commandHandler;
    private static RobotSensorsData robotSensorsData;

    public static void main(String[] args) throws IOException, TimeoutException {
        robotSensorsData = new RobotSensorsData();
        commandHandler = new CommandHandler(robotSensorsData);
        communicationHandler = new CommunicationHandler(MainTest::onReceiveCallback);

//        String json = "{\"EV3\": {1: {}, 2 : {\"B\": 32, \"C\": 31}}}";
//        commandHandler.getCommand("\"Drive\"").executeCommand(json);
        communicationHandler.openQueues();

//        while (true){
//
//            if (robotSensorsData.isUpdated()) {
//                String json = robotSensorsData.toJson();
//                communicationHandler.send(json);
//            }
//        }
    }

    private static void onReceiveCallback(String consumerTag, Delivery delivery) throws IOException {

        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        System.out.println(" [x] Received '" + message + "'");

        MessageContent messageContent = commandHandler.parseCommand(message);

        commandHandler.getCommand(messageContent.getCommand()).executeCommand(messageContent.getData());
    }

}
