import Communication.CommunicationHandler;
import RobotData.RobotSensorsData;
import com.rabbitmq.client.Delivery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class MainTest {

    private static CommandHandler commandHandler;

    public static void main(String[] args) throws IOException, TimeoutException {
        RobotSensorsData robotSensorsData = new RobotSensorsData();
        commandHandler = new CommandHandler(robotSensorsData);
        CommunicationHandler communicationHandler = new CommunicationHandler("Data", "Commands");
        communicationHandler.setMyCallback(MainTest::onReceiveCallback);

//        String json = "{\"EV3\": {1: {}, 2 : {\"B\": 32, \"C\": 31}}}";
//        commandHandler.getCommand("\"Drive\"").executeCommand(json);
        communicationHandler.openSendQueue(true);
        communicationHandler.openReceiveQueue(false);

        while (true){

            if (robotSensorsData.isUpdated()) {
                String json = robotSensorsData.toJson();
                communicationHandler.send(json);
            }
        }
    }

    private static void onReceiveCallback(String consumerTag, Delivery delivery) throws IOException {

        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//        System.out.println(" [x] Received '" + message + "'");
//        System.out.println("Msg no. " + delivery.getProperties().getMessageId());
        commandHandler.parseAndExecuteCommand(message);
    }

}
