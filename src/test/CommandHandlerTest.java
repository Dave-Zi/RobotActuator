import Boards.IBoard;
import Boards.TestBoard;
import Enums.*;
import RobotData.RobotSensorsData;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class CommandHandlerTest {
    private TestCommandHandler emptyCommandHandler;
    private TestCommandHandler commandHandler;
    private RobotSensorsData robotSensorsData;

    // ------------- Subscribe -------------

    @org.junit.Test
    public void subscribeTest() {
        try {

            robotSensorsData = new RobotSensorsData();
            emptyCommandHandler = new TestCommandHandler(robotSensorsData);
            emptyCommandHandler.executeCommand("\"Build\"", "");

            assertNull(emptyCommandHandler.getRobotSensorsData().getPorts("EV3", "_1"));
            assertNull(emptyCommandHandler.getRobotSensorsData().getPorts("EV3", "_2"));
            assertNull(emptyCommandHandler.getRobotSensorsData().getPorts("GrovePi", "_1"));

            String dataToSubscribe = "{\"EV3\": {\"1\": [\"D\"],\"2\": [\"3\"]},\"GrovePi\": [\"D8\"]}";
            emptyCommandHandler.executeCommand("\"Subscribe\"", dataToSubscribe);

            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("D"));
            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("EV3", "_2").contains("_3"));
            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("D8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @org.junit.Test
    public void subscribeWithoutIndexTest() {
        try {
            robotSensorsData = new RobotSensorsData();
            emptyCommandHandler = new TestCommandHandler(robotSensorsData);
            emptyCommandHandler.executeCommand("\"Build\"", "");

            assertNull(emptyCommandHandler.getRobotSensorsData().getPorts("EV3", "_1"));
            assertNull(emptyCommandHandler.getRobotSensorsData().getPorts("GrovePi", "_1"));

            String DataToSubscribe = "{\"EV3\": [\"1\", \"D\"], \"GrovePi\": [\"D4\", \"A0\"]}";
            emptyCommandHandler.executeCommand("\"Subscribe\"", DataToSubscribe);

            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("_1"));
            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("D"));
            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("D4"));
            assertTrue(emptyCommandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("A0"));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


// ------------- Unsubscribe -------------

    @org.junit.Test
    public void unsubscribeTest() throws IOException {
        robotSensorsData = new RobotSensorsData();
        commandHandler = new TestCommandHandler(robotSensorsData);
        commandHandler.executeCommand("\"Build\"", "");
        String boardsMapData = "{\"EV3\": {\"1\": [\"A\", \"B\"],\"2\": [\"2\", \"B\"]},\"GrovePi\": { \"1\": [\"D2\", \"A1\", \"D4\"], \"4\": [\"D2\"]}}";
        commandHandler.robotSensorsData.addToBoardsMap(boardsMapData);

        // Unsubscribe with index
        String dataToUnsubscribe = "{\"EV3\": {\"1\": [\"A\"], \"2\": [\"2\"]},\"GrovePi\": [\"D2\"]}";
        assertTrue(commandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("A"));
        assertTrue(commandHandler.getRobotSensorsData().getPorts("EV3", "_2").contains("_2"));
        assertTrue(commandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("D2"));
        try {
            commandHandler.executeCommand("\"Unsubscribe\"", dataToUnsubscribe);

            assertFalse(commandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("A"));
            assertFalse(commandHandler.getRobotSensorsData().getPorts("EV3", "_2").contains("_2"));
            assertFalse(commandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("D2"));

            //check the rest were not removed
            assertTrue(commandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("B"));
            assertTrue(commandHandler.getRobotSensorsData().getPorts("EV3", "_2").contains("B"));
            assertTrue(commandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("A1"));
            assertTrue(commandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("D4"));
            assertTrue(commandHandler.getRobotSensorsData().getPorts("GrovePi", "_4").contains("D2"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @org.junit.Test
    public void unsubscribeWithoutIndexTest() throws IOException {
        robotSensorsData = new RobotSensorsData();
        commandHandler = new TestCommandHandler(robotSensorsData);
        commandHandler.executeCommand("\"Build\"", "");
        String boardsMapData = "{\"EV3\": {\"1\": [\"A\", \"B\"],\"2\": [\"2\", \"B\"]},\"GrovePi\": { \"1\": [\"D2\", \"A1\", \"D4\"], \"4\": [\"D2\"]}}";
        commandHandler.robotSensorsData.addToBoardsMap(boardsMapData);

        // unsubscribe without index
        assertTrue(commandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("B"));
        assertTrue(commandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("D4"));

        String dataUnsubscribe = "{\"EV3\": [\"B\"], \"GrovePi\": [\"D4\"]}";
        try {
            commandHandler.executeCommand("\"Unsubscribe\"", dataUnsubscribe);
            assertFalse(commandHandler.getRobotSensorsData().getPorts("EV3", "_1").contains("B"));
            assertFalse(commandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("D4"));

            //check the rest were not removed
            assertTrue(commandHandler.getRobotSensorsData().getPorts("EV3", "_2").contains("B"));
            assertTrue(commandHandler.getRobotSensorsData().getPorts("GrovePi", "_1").contains("A1"));
            assertTrue(commandHandler.getRobotSensorsData().getPorts("GrovePi", "_4").contains("D2"));
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    // -------------------- Drive -----------------
    @org.junit.Test
    public void driveTest() throws IOException {
        robotSensorsData = new RobotSensorsData();
        emptyCommandHandler = new TestCommandHandler(new RobotSensorsData());

        emptyCommandHandler.executeCommand("\"Build\"", "");

        String dataToSubscribe = "{\"EV3\": {\"2\": [\"C\", \"D\"],\"4\": [\"2\", \"B\"]}}";
        emptyCommandHandler.executeCommand("\"Subscribe\"", dataToSubscribe);

        // Check there was no value before drive
        RobotSensorsData _robotSensorsData = emptyCommandHandler.getRobotSensorsData();
        HashMap<String, Double> portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_2"));

        assertNull(portsAndValues.get("C"));
        assertNull(portsAndValues.get("D"));

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_4"));
        assertNull(portsAndValues.get("B"));


        String sensorsToDrive = "{\"EV3\": {\"2\": {\"C\": 10, \"D\": 10}, \"4\":{\"B\": 10}}}";
        emptyCommandHandler.executeCommand("\"Drive\"", sensorsToDrive);
        _robotSensorsData = emptyCommandHandler.getRobotSensorsData();

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_2"));

        assertEquals(portsAndValues.get("C"), 10, 0.01);
        assertEquals(portsAndValues.get("D"), 10, 0.01);

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_4"));

        assertEquals(portsAndValues.get("B"), 10, 0.01);
    }


    @org.junit.Test
    public void driveWithoutIndexTest() throws IOException {
        robotSensorsData = new RobotSensorsData();
        emptyCommandHandler = new TestCommandHandler(new RobotSensorsData());

        emptyCommandHandler.executeCommand("\"Build\"", "");

        String dataForDrive = "{\"EV3\": {\"1\": [\"A\", \"B\"]}}";
        emptyCommandHandler.executeCommand("\"Subscribe\"", dataForDrive);

        // Check there was no value before drive
        RobotSensorsData _robotSensorsData = emptyCommandHandler.getRobotSensorsData();
        HashMap<String, Double> portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_1"));

        assertNull(portsAndValues.get("A"));
        assertNull(portsAndValues.get("B"));

        String sensorsToDrive = "{\"EV3\": {\"A\": 10, \"B\": 10}}";
        emptyCommandHandler.executeCommand("\"Drive\"", sensorsToDrive);
        _robotSensorsData = emptyCommandHandler.getRobotSensorsData();

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_1"));

        assertEquals(portsAndValues.get("A"), 10, 0.01);
        assertEquals(portsAndValues.get("B"), 10, 0.01);
    }


    // -------------------- Rotate -----------------
    @org.junit.Test
    public void rotateTest() throws IOException {
        robotSensorsData = new RobotSensorsData();
        emptyCommandHandler = new TestCommandHandler(new RobotSensorsData());

        emptyCommandHandler.executeCommand("\"Build\"", "");

        String dataToSubscribe = "{\"EV3\": {\"2\": [\"C\", \"D\"],\"4\": [\"2\", \"B\"]}}";
        emptyCommandHandler.executeCommand("\"Subscribe\"", dataToSubscribe);

        // Check there was no value before rotate
        RobotSensorsData _robotSensorsData = emptyCommandHandler.getRobotSensorsData();
        HashMap<String, Double> portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_2"));

        assertNull(portsAndValues.get("C"));
        assertNull(portsAndValues.get("D"));

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_4"));
        assertNull(portsAndValues.get("B"));

        String sensorsToRotate = "{\"EV3\": {\"2\":{\"C\": 90, \"D\": 90,\"speed\": 10},\"4\":{\"B\": 50,\"speed\": 10}}}";
        emptyCommandHandler.executeCommand("\"Rotate\"", sensorsToRotate);
        _robotSensorsData = emptyCommandHandler.getRobotSensorsData();

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_2"));

        assertEquals(portsAndValues.get("C"), 10, 0.01);
        assertEquals(portsAndValues.get("D"), 10, 0.01);

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_4"));

        assertEquals(portsAndValues.get("B"), 10, 0.01);
    }


    @org.junit.Test
    public void rotateWithoutIndexTest() throws IOException {
        robotSensorsData = new RobotSensorsData();
        emptyCommandHandler = new TestCommandHandler(new RobotSensorsData());

        emptyCommandHandler.executeCommand("\"Build\"", "");

        String dataForDrive = "{\"EV3\": {\"1\": [\"A\", \"B\"]}}";
        emptyCommandHandler.executeCommand("\"Subscribe\"", dataForDrive);

        // Check there was no value before rotate
        RobotSensorsData _robotSensorsData = emptyCommandHandler.getRobotSensorsData();
        HashMap<String, Double> portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_1"));

        assertNull(portsAndValues.get("A"));
        assertNull(portsAndValues.get("B"));

        String sensorsToRotate = "{\"EV3\": {\"A\": 90, \"B\": 90,\"speed\": 10}}";
        emptyCommandHandler.executeCommand("\"Rotate\"", sensorsToRotate);
        _robotSensorsData = emptyCommandHandler.getRobotSensorsData();

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_1"));

        assertEquals(portsAndValues.get("A"), 10, 0.01);
        assertEquals(portsAndValues.get("B"), 10, 0.01);
    }


    // -------------------- Set Sensor Mode -----------------
    @org.junit.Test
    public void setSensorModeTest() throws IOException {
        robotSensorsData = new RobotSensorsData();
        emptyCommandHandler = new TestCommandHandler(new RobotSensorsData());

        emptyCommandHandler.executeCommand("\"Build\"", "");

        String dataToSubscribe = "{\"EV3\": {\"2\": [\"2\", \"4\"],\"4\": [\"2\", \"3\"]}, \"GrovePi\": { \"1\": [\"D2\", \"A1\", \"D4\"], \"4\": [\"D2\"]}}";
        emptyCommandHandler.executeCommand("\"Subscribe\"", dataToSubscribe);

        // Check there was no value before rotate
        RobotSensorsData _robotSensorsData = emptyCommandHandler.getRobotSensorsData();
        HashMap<String, Double> portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_2"));

        assertNull(portsAndValues.get("_2"));
        assertNull(portsAndValues.get("_4"));

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("GrovePi", "_1"));
        assertNull(portsAndValues.get("A1"));

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("GrovePi", "_4"));
        assertNull(portsAndValues.get("D2"));


        String setSensorMode = "{\"EV3\": {\"2\":{\"2\": 0.0, \"4\": 1.0},\"4\":{\"2\": 0.0}},\"GrovePi\": {\"1\":{\"A1\": 0.0},\"4\":{\"D2\": 1.0}}}";
        emptyCommandHandler.executeCommand("\"SetSensorMode\"", setSensorMode);

        _robotSensorsData = emptyCommandHandler.getRobotSensorsData();

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_2"));

        assertEquals(portsAndValues.get("_2"), 0.0, 0.01);
        assertEquals(portsAndValues.get("_4"), 1.0, 0.01);

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_4"));

        assertEquals(portsAndValues.get("_2"), 0.0, 0.01);

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("GrovePi", "_1"));

        assertEquals(portsAndValues.get("A1"), 0.0, 0.01);

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("GrovePi", "_4"));

        assertEquals(portsAndValues.get("D2"), 1.0, 0.01);
    }

    @org.junit.Test
    public void setSensorModeWithoutIndexTest() throws IOException {
        robotSensorsData = new RobotSensorsData();
        emptyCommandHandler = new TestCommandHandler(new RobotSensorsData());

        emptyCommandHandler.executeCommand("\"Build\"", "");

        String dataToSubscribe = "{\"EV3\": [\"1\", \"4\"], \"GrovePi\":[\"D2\", \"A1\"]}";
        emptyCommandHandler.executeCommand("\"Subscribe\"", dataToSubscribe);

        // Check there was no value before rotate
        RobotSensorsData _robotSensorsData = emptyCommandHandler.getRobotSensorsData();
        HashMap<String, Double> portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_1"));

        assertNull(portsAndValues.get("_1"));
        assertNull(portsAndValues.get("_4"));

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("GrovePi", "_1"));

        assertNull(portsAndValues.get("D2"));
        assertNull(portsAndValues.get("A1"));

        String setSensorMode = "{\"EV3\": {\"1\": 0.0, \"4\": 1.0}, \"GrovePi\": {\"D2\": 1.0, \"A1\": 0.0}}";
        emptyCommandHandler.executeCommand("\"SetSensorMode\"", setSensorMode);

        _robotSensorsData = emptyCommandHandler.getRobotSensorsData();

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_1"));

        assertEquals(portsAndValues.get("_1"), 0.0, 0.01);
        assertEquals(portsAndValues.get("_4"), 1.0, 0.01);

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("GrovePi", "_1"));

        assertEquals(portsAndValues.get("D2"), 1.0, 0.01);
        assertEquals(portsAndValues.get("A1"), 0.0, 0.01);
    }


    // -------------------- Set Sensor -----------------
    @org.junit.Test
    public void setActuatorDataTest() throws IOException {
        robotSensorsData = new RobotSensorsData();
        emptyCommandHandler = new TestCommandHandler(new RobotSensorsData());

        emptyCommandHandler.executeCommand("\"Build\"", "");

        String dataToSubscribe = "{\"EV3\": {\"2\": [\"2\", \"4\"],\"4\": [\"2\", \"3\"]}, \"GrovePi\": { \"1\": [\"D2\", \"A1\", \"D4\"], \"4\": [\"D2\"]}}";
        emptyCommandHandler.executeCommand("\"Subscribe\"", dataToSubscribe);

        // Check there was no value before rotate
        RobotSensorsData _robotSensorsData = emptyCommandHandler.getRobotSensorsData();
        HashMap<String, Double> portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_2"));

        assertNull(portsAndValues.get("_2"));
        assertNull(portsAndValues.get("_4"));

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("GrovePi", "_1"));
        assertNull(portsAndValues.get("A1"));

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("GrovePi", "_4"));
        assertNull(portsAndValues.get("D2"));


        String setActuatorData = "{\"EV3\": {\"2\":{\"2\": 0.0, \"4\": 1.0},\"4\":{\"2\": 0.0}},\"GrovePi\": {\"1\":{\"A1\": 0.0},\"4\":{\"D2\": 1.0}}}";
        emptyCommandHandler.executeCommand("\"SetActuatorData\"", setActuatorData);

        _robotSensorsData = emptyCommandHandler.getRobotSensorsData();

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_2"));

        assertEquals(portsAndValues.get("_2"), 0.0, 0.01);
        assertEquals(portsAndValues.get("_4"), 1.0, 0.01);

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_4"));

        assertEquals(portsAndValues.get("_2"), 0.0, 0.01);

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("GrovePi", "_1"));

        assertEquals(portsAndValues.get("A1"), 0.0, 0.01);

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("GrovePi", "_4"));

        assertEquals(portsAndValues.get("D2"), 1.0, 0.01);
    }


    @org.junit.Test
    public void setActuatorDataWithoutIndexTest() throws IOException {
        robotSensorsData = new RobotSensorsData();
        emptyCommandHandler = new TestCommandHandler(new RobotSensorsData());

        emptyCommandHandler.executeCommand("\"Build\"", "");

        String dataToSubscribe = "{\"EV3\": [\"1\", \"4\"], \"GrovePi\":[\"D2\", \"A1\"]}";
        emptyCommandHandler.executeCommand("\"Subscribe\"", dataToSubscribe);

        // Check there was no value before rotate
        RobotSensorsData _robotSensorsData = emptyCommandHandler.getRobotSensorsData();
        HashMap<String, Double> portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_1"));

        assertNull(portsAndValues.get("_1"));
        assertNull(portsAndValues.get("_4"));

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("GrovePi", "_1"));

        assertNull(portsAndValues.get("D2"));
        assertNull(portsAndValues.get("A1"));

        String setActuatorData = "{\"EV3\": {\"1\": 0.0, \"4\": 1.0}, \"GrovePi\": {\"D2\": 1.0, \"A1\": 0.0}}";
        emptyCommandHandler.executeCommand("\"SetActuatorData\"", setActuatorData);

        _robotSensorsData = emptyCommandHandler.getRobotSensorsData();

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("EV3", "_1"));

        assertEquals(portsAndValues.get("_1"), 0.0, 0.01);
        assertEquals(portsAndValues.get("_4"), 1.0, 0.01);

        portsAndValues = new HashMap<>(_robotSensorsData.getPortsAndValues("GrovePi", "_1"));

        assertEquals(portsAndValues.get("D2"), 1.0, 0.01);
        assertEquals(portsAndValues.get("A1"), 0.0, 0.01);
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
        ev3PortsMap1.put(Ev3SensorPort._1, 1.0);
        ev3PortsMap1.put(Ev3SensorPort._4, 0.0);
        Map<IPortEnums, Double> ev3PortsMap2 = new HashMap<>();
        ev3PortsMap2.put(Ev3DrivePort.C, 10.0);
        ev3PortsMap2.put(Ev3DrivePort.D, 10.0);
        ev3PortsMap2.put(Ev3SensorPort._2, 0.0);
        ev3PortsMap2.put(Ev3SensorPort._4, 1.0);
        Map<IPortEnums, Double> ev3PortsMap4 = new HashMap<>();
        ev3PortsMap4.put(Ev3DrivePort.B, 10.0);
        ev3PortsMap4.put(Ev3SensorPort._3, 1.0);


        Map<IPortEnums, Double> grovePiPortsMap = new HashMap<>();
        grovePiPortsMap.put(GrovePiPort.D2, 1.0);
        grovePiPortsMap.put(GrovePiPort.D3, 1.0);
        grovePiPortsMap.put(GrovePiPort.A1, 0.0);


        Map<IPortEnums, Double> grovePiPortsMap4 = new HashMap<>();
        grovePiPortsMap4.put(GrovePiPort.D2, 1.0);


        Map<Integer, IBoard> ev3 = Map.of(1, new TestBoard(ev3PortsMap1), 2, new TestBoard(ev3PortsMap2), 4, new TestBoard(ev3PortsMap4));
        Map<Integer, IBoard> grovePi = Map.of(1, new TestBoard(grovePiPortsMap),4, new TestBoard(grovePiPortsMap4));

        Map<BoardTypeEnum, Map<Integer, IBoard>> robot = new HashMap<>();
        robot.put(BoardTypeEnum.EV3, ev3);
        robot.put(BoardTypeEnum.GrovePi, grovePi);

        this.setRobot(robot);
    }

    RobotSensorsData getRobotSensorsData() {
        return robotSensorsData;
    }


    private void setRobot(Map<BoardTypeEnum, Map<Integer, IBoard>> robot) {
        this.robot = robot;
    }

}