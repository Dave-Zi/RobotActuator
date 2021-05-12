import Boards.IBoard;
import Boards.TestBoard;
import Enums.*;
import RobotData.RobotSensorsData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CommandHandlerTest {


    private TestCommandHandler emptyCommandHandler;
    private TestCommandHandler commandHandler;

    private RobotSensorsData robotSensorsData;

    @org.junit.Before
    public void setUp() throws IOException {

        robotSensorsData = new RobotSensorsData();
        String boardsMapData = "{\"EV3\": {\"1\": [\"A\"],\"2\": [\"2\"]},\"GrovePi\": [\"D2\"]}";
        robotSensorsData.addToBoardsMap(boardsMapData);
        commandHandler = new TestCommandHandler(robotSensorsData);
        String robotJson = "{\"EV3\":[{\"Port\": \"rfcomm0\"}],\"GrovePi\":[{\"A0\": \"\",\"A1\": \"\",\"A2\":\"\",\"D2\": \"Led\",\"D3\": \"\",\"D4\": \"Ultrasonic\",\"D5\": \"\",\"D6\": \"\",\"D7\": \"\",\"D8\": \"Led\"}]}";
        commandHandler.executeCommand("\"Build\"", robotJson);

        emptyCommandHandler = new TestCommandHandler(new RobotSensorsData());
        emptyCommandHandler.executeCommand("\"Build\"", robotJson);
    }

    @org.junit.After
    public void tearDown() {
        commandHandler = new TestCommandHandler(new RobotSensorsData());
        emptyCommandHandler = new TestCommandHandler(new RobotSensorsData());
        robotSensorsData.clear();
    }

    // ------------- Subscribe -------------
    @org.junit.Test
    public void subscribeWithoutIndexTest() {
        try {
            String DataToSubscribe = "{\"EV3\": [\"1\"]}";
            emptyCommandHandler.executeCommand("\"Subscribe\"", DataToSubscribe);
            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("_1"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @org.junit.Test
    public void subscribeWithIndexTest() {
        try {
            String dataToSubscribe = "{\"EV3\": {\"1\": [\"C\"],\"2\": [\"3\"]},\"GrovePi\": [\"D3\"]}";
            emptyCommandHandler.executeCommand("\"Subscribe\"", dataToSubscribe);
            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("C"));
            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("EV3", "_2").contains("_3"));
            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("D3"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

// ------------- Unsubscribe -------------

    @org.junit.Test
    public void unsubscribeWithIndexTest() {
        String dataToUnsubscribe = "{\"EV3\": {\"2\": [\"2\"]},\"GrovePi\": [\"D2\"]}";
        assertTrue(commandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("A"));
        assertTrue(commandHandler.getRobotSensorsData().getPorts("EV3", "_2").contains("_2"));
        assertTrue(commandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("D2"));
        try {
            commandHandler.executeCommand("\"Unsubscribe\"", dataToUnsubscribe);
            assertFalse(commandHandler.getRobotSensorsData().getPorts("EV3", "_2").contains("_2"));
            assertFalse(commandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("D2"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @org.junit.Test
    public void unsubscribeWithoutIndexTest() throws IOException {

        String dataToUnsubscribe = "{\"EV3\": [\"A\"]}";
        assertTrue(commandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("A"));
        commandHandler.executeCommand("\"Unsubscribe\"", dataToUnsubscribe);
        assertFalse(commandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("A"));
    }

    // -------------------- Drive -----------------
    @org.junit.Test
    public void driveTest() throws IOException {

        String dataForDrive = "{\"EV3\": {\"1\": [\"B\"]}}";
        emptyCommandHandler.executeCommand("\"Subscribe\"", dataForDrive);
        String sensorsToDrive = "{\"EV3\": {\"B\": 10}}";
        emptyCommandHandler.executeCommand("\"Drive\"", sensorsToDrive);
        RobotSensorsData _robotSensorsData = emptyCommandHandler.getRobotSensorsData();

        HashMap<String, Double> portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_1"));
        assertEquals(portsAndValues.get("B"), 10, 0.01);
    }

    // -------------------- Rotate -----------------
    @org.junit.Test
    public void rotateTest() throws IOException {

        String dataForRotate = "{\"EV3\": {\"1\": [\"B\"]}}";
        emptyCommandHandler.executeCommand("\"Subscribe\"", dataForRotate);
        String sensorsToRotate = "{\"EV3\": {\"1\":{\"B\": 90, \"speed\": 10}}}";
        emptyCommandHandler.executeCommand("\"Rotate\"", sensorsToRotate);
        RobotSensorsData _robotSensorsData = emptyCommandHandler.getRobotSensorsData();

        HashMap<String, Double> portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_1"));
        assertEquals(portsAndValues.get("B"), 10, 0.01);
    }

    // -------------------- Set Sensor -----------------
    @org.junit.Test
    public void setSensorTest() throws IOException {
        String dataForSensor = "{\"EV3\": {\"1\": [\"B\"]}}";
        emptyCommandHandler.executeCommand("\"Subscribe\"", dataForSensor);
        String sensorsToSet = "{\"EV3\": {\"B\": 1.0}}";
        emptyCommandHandler.executeCommand("\"SetSensorMode\"", sensorsToSet);
        RobotSensorsData _robotSensorsData = emptyCommandHandler.getRobotSensorsData();

        HashMap<String, Double> portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_1"));
        assertEquals(portsAndValues.get("B"), 1.0, 0.01);
    }
}

class TestCommandHandler extends CommandHandler {

    TestCommandHandler(RobotSensorsData robotSensorsData) {
        super(robotSensorsData);
    }

    @Override
    void build(String json) {
        Map<IPortEnums, Double> ev3PortsMap1 = new HashMap<>();
        ev3PortsMap1.put(Ev3DrivePort.A, 10.0);
        ev3PortsMap1.put(Ev3DrivePort.B, 10.0);
        ev3PortsMap1.put(Ev3DrivePort.C, 10.0);
        ev3PortsMap1.put(Ev3SensorPort._1, 10.0);
        ev3PortsMap1.put(Ev3SensorPort._4, 10.0);
        Map<IPortEnums, Double> ev3PortsMap2 = new HashMap<>();
        ev3PortsMap2.put(Ev3SensorPort._2, 10.0);
        ev3PortsMap2.put(Ev3SensorPort._3, 10.0);
        Map<IPortEnums, Double> grovePiPortsMap = new HashMap<>();
        grovePiPortsMap.put(GrovePiPort.D2, 1.0);
        grovePiPortsMap.put(GrovePiPort.D3, 1.0);

        Map<Integer, IBoard> ev3 = Map.of(1, new TestBoard(ev3PortsMap1), 2, new TestBoard(ev3PortsMap2));
        Map<Integer, IBoard> grovePi = Map.of(1, new TestBoard(grovePiPortsMap));

        Map<BoardTypeEnum, Map<Integer, IBoard>> robot = new HashMap<>();
        robot.put(BoardTypeEnum.EV3, ev3);
        robot.put(BoardTypeEnum.GrovePi, grovePi);

        this.setRobot(robot);
    }
}