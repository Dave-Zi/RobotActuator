import Communication.CommunicationHandler;
import Communication.ICommunication;
import Communication.QueueNameEnum;
import RobotData.RobotSensorsData;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Delivery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class MainTest {

    private static CommandHandler commandHandler;
    private static ICommunication communicationHandler;

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws IOException, TimeoutException {
        RobotSensorsData robotSensorsData = new RobotSensorsData();
        commandHandler = new CommandHandler(robotSensorsData);
        communicationHandler = new CommunicationHandler();
        communicationHandler.connect();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                communicationHandler.closeConnection();
                commandHandler.closeBoards();
            } catch (IOException | TimeoutException | AlreadyClosedException ignore) { }
            System.out.println("Connection Closed!");

        }));

//        String json = "{\"EV3\": {1: {}, 2 : {\"B\": 32, \"C\": 31}}}";
//        commandHandler.getCommand("\"Drive\"").executeCommand(json);

        // Sending on Data and Free.
        // Listening on Commands and SOS.
        communicationHandler.purgeQueue(QueueNameEnum.Data);
        communicationHandler.purgeQueue(QueueNameEnum.Free);
        communicationHandler.consumeFromQueue(QueueNameEnum.Commands, MainTest::onReceiveCallback);
        communicationHandler.consumeFromQueue(QueueNameEnum.SOS, MainTest::onReceiveCallback);

        while (true){
            if (robotSensorsData.isUpdated()) {
                String json = robotSensorsData.toJson();
                communicationHandler.send(json, QueueNameEnum.Data);
            }
        }
    }

    private static void onReceiveCallback(String consumerTag, Delivery delivery) throws IOException {

        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        JsonObject obj = new JsonParser().parse(message).getAsJsonObject();
        String command = String.valueOf(obj.get("Command"));
        String dataJsonString = String.valueOf(obj.get("Data"));

        if (command.equals("\"MyAlgorithm\"")){
            String result = commandHandler.executeAlgorithm(dataJsonString);
            communicationHandler.send(result, QueueNameEnum.Free);

        } else {
            commandHandler.executeCommand(command, dataJsonString);
        }

    }

}
