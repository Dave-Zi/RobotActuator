import Enums.BoardTypeEnum;
import RobotData.RobotSensorsData;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
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
    private final String dataForDrive = "{\"EV3\": [\"B\"]}";
    private final String driveJsonNoIndex = "{\"EV3\": {\"B\": 10}}";
    private final String rotateJsonNoIndex = "{\"EV3\": {\"B\": 90, \"speed\": 10}}";

    private final String setSensorJsonNoIndex = "{\"EV3\": {\"B\": 1.0}}";


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
            emptyCommandHandler.executeCommand("\"Subscribe\"", dataWithoutIndex);
            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("_2"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @org.junit.Test
    public void subscribeWithIndexTest() {
        try {
            emptyCommandHandler.executeCommand("\"Subscribe\"", dataWithIndex);
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
            commandHandler.executeCommand("\"Unsubscribe\"", dataWithIndex);
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
      //  robot = commandHandler.getRobot();
        // TODO equal between the maps
    }

    // -------------------- Drive -----------------
    @org.junit.Test
    public void driveTest() throws IOException {
        emptyCommandHandler.executeCommand("\"Build\"",robotJson);
        emptyCommandHandler.executeCommand("\"Subscribe\"", dataForDrive);
        emptyCommandHandler.executeCommand("\"Drive\"",driveJsonNoIndex);
        RobotSensorsData _robotSensorsData = emptyCommandHandler.getRobotSensorsData();

        HashMap<String, Double> portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_1"));
        assertEquals(portsAndValues.get("B"), 10, 0.01);
    }

    // -------------------- Rotate -----------------
    @org.junit.Test
    public void rotateTest() throws IOException {
        emptyCommandHandler.executeCommand("\"Build\"",robotJson);
        emptyCommandHandler.executeCommand("\"Subscribe\"", dataForDrive);
        emptyCommandHandler.executeCommand("\"Rotate\"",rotateJsonNoIndex);
        RobotSensorsData _robotSensorsData = emptyCommandHandler.getRobotSensorsData();

        HashMap<String, Double> portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_1"));
        assertEquals(portsAndValues.get("B"), 10, 0.01);
    }

    // -------------------- Set Sensor -----------------
    @org.junit.Test
    public void setSensorTest() throws IOException {
        emptyCommandHandler.executeCommand("\"Build\"",robotJson);
        emptyCommandHandler.executeCommand("\"Subscribe\"", dataForDrive);
        emptyCommandHandler.executeCommand("\"SetSensor\"",setSensorJsonNoIndex);
        RobotSensorsData _robotSensorsData = emptyCommandHandler.getRobotSensorsData();

        HashMap<String, Double> portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_1"));
//        MockBoard ev3 = (MockBoard) emptyCommandHandler.getRobot().get(BoardTypeEnum.EV3).get(1);

        assertEquals(portsAndValues.get("B"), 1.0, 0.01);
    }
}