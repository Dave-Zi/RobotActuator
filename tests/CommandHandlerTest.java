import Enums.BoardTypeEnum;
import RobotData.RobotSensorsData;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class CommandHandlerTest {

    private HashMap<BoardTypeEnum, List<IBoard>> robot;
    private CommandHandler commandHandler;
    private CommandHandler emptyCommandHandler;

    private final String dataWithIndex = "{\"EV3\": {\"1\": [\"2\"],\"2\": [\"3\"]},\"GrovePi\": [\"D3\"]}";
    private final String dataWithoutIndex = "{\"EV3\": [\"2\"]}";
    private final String robotJson = "{\"EV3\":[{\"Port\": \"rfcomm0\"}],\"GrovePi\":[{\"A0\": \"\",\"A1\": \"\",\"A2\":\"\",\"D2\": \"Led\",\"D3\": \"\",\"D4\": \"Ultrasonic\",\"D5\": \"\",\"D6\": \"\",\"D7\": \"\",\"D8\": \"Led\"}]}";
    private final String driveJson = "{\"EV3\": {\"B\": 10, \"C\": 10}}";
    private final JsonElement jsonElement = new JsonParser().parse(robotJson);
    private JsonObject jsonObject = new JsonObject();

    @org.junit.Before
    public void setUp() {
        RobotSensorsData robotSensorsData = new RobotSensorsData();
        robotSensorsData.addToBoardsMap(dataWithIndex);
        robotSensorsData.addToBoardsMap(dataWithoutIndex);
        commandHandler = new CommandHandler(robotSensorsData);

        jsonObject.addProperty("json", robotJson);

        RobotSensorsData emptyRobotSensorsData = new RobotSensorsData();
        emptyCommandHandler = new CommandHandler(emptyRobotSensorsData);
    }

    @org.junit.After
    public void tearDown() {
        commandHandler.setRobotSensorsData(new RobotSensorsData());
    }

    // ------------- Subscribe -------------
    @org.junit.Test
    public void subscribeWithoutIndexTest() {
        try {
            emptyCommandHandler.commandToMethod.get("\"Subscribe\"").executeCommand(dataWithoutIndex);
            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("_2"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @org.junit.Test
    public void subscribeWithIndexTest() {
        try {
            emptyCommandHandler.commandToMethod.get("\"Subscribe\"").executeCommand(dataWithIndex);
            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("_2"));
            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("EV3", "_2").contains("_3"));
            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("D3"));


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

// ------------- Unsubscribe -------------

    @org.junit.Test
    public void unsubscribeWithIndexTest() {
        assertTrue(commandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("_2"));
        assertTrue(commandHandler.getRobotSensorsData().getPorts("EV3", "_2").contains("_3"));
        assertTrue(commandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("D3"));

        try {
            commandHandler.commandToMethod.get("\"Unsubscribe\"").executeCommand(dataWithIndex);
            assertFalse(commandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("_2"));
            assertFalse(commandHandler.getRobotSensorsData().getPorts("EV3", "_2").contains("_3"));
            assertFalse(commandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("D3"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @org.junit.Test
    public void unsubscribeWithoutIndexTest() throws IOException {
        assertTrue(commandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("_2"));
        commandHandler.commandToMethod.get("\"Unsubscribe\"").executeCommand(dataWithoutIndex);
        assertFalse(commandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("_2"));
    }


    // -------------------- Build -----------------
    @org.junit.Test
    public void buildTest() throws IOException {
//        commandHandler.commandToMethod.get("\"Build\"").executeCommand(robotJson);
        robot = commandHandler.getRobot();
        // TODO equal between the maps
    }

    // -------------------- Drive -----------------
    @org.junit.Test
    public void driveTest() throws IOException {
        //todo send another string
//        commandHandler.commandToMethod.get("\"Drive\"").executeCommand(robotJson);
        robot = commandHandler.getRobot();
        // TODO equal
    }

    // -------------------- Rotate -----------------
    @org.junit.Test
    public void rotateTest() throws IOException {
        //todo send another string
//        commandHandler.commandToMethod.get("\"Rotate\"").executeCommand(robotJson);
        robot = commandHandler.getRobot();
        // TODO equal
    }

    // -------------------- Set Sensor -----------------
    @org.junit.Test
    public void setSensorTest() throws IOException {
        //todo send another string
//        commandHandler.commandToMethod.get("\"SetSensor\"").executeCommand(robotJson);
        robot = commandHandler.getRobot();
        // TODO equal
    }
}