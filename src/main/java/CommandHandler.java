import Enums.BoardTypeEnum;
import Enums.IPortEnums;
import RobotData.RobotSensorsData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CommandHandler {

    private RobotSensorsData robotSensorsData;
    private Map<BoardTypeEnum, Map<Integer, IBoard>> robot;
    private final int commandTimeout = 150;

    // Uniform Interface for commands arriving from BPjs
    private ICommand subscribe = this::subscribe;
    private ICommand unsubscribe = this::unsubscribe;
    private ICommand build = this::build;
    private ICommand drive = this::drive;
    private ICommand rotate = this::rotate;
    private ICommand setSensor = this::setSensor;


    // Thread for data collection from robot sensors
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private Future dataCollectionFuture;

    private Map<String, ICommand> commandToMethod = Stream.of(new Object[][]{
            {"\"Subscribe\"", subscribe},
            {"\"Unsubscribe\"", unsubscribe},
            {"\"Build\"", build},
            {"\"Drive\"", drive},
            {"\"Rotate\"", rotate},
            {"\"SetSensor\"", setSensor}

    }).collect(Collectors.toMap(data -> (String) data[0], data -> (ICommand) data[1]));

    CommandHandler(RobotSensorsData robotSensorsData){
        this.robotSensorsData = robotSensorsData;
    }

    // Parse & execute command from message that arrived from BPjs
    void parseAndExecuteCommand(String message) throws IOException {
        JsonObject obj = new JsonParser().parse(message).getAsJsonObject();
        String command = String.valueOf(obj.get("Command"));
        String dataJsonString = String.valueOf(obj.get("Data"));

        ICommand commandToExecute = commandToMethod.get(command);
        commandToExecute.executeCommand(dataJsonString);
    }

    /**
     * Subscribe to new ports.
     *
     * 1. Stop data collection from ports
     * 2. Add new ports to Robot Sensor Data Object.
     * 3. Restart Data Collection Thread.
     *
     * @param json string from BPjs messages
     */
    private void subscribe(String json){
        System.out.println("in subscribe!");
        robotSensorsData.addToBoardsMap(json);
        startExecutor();
    }
    /**
     * Unsubscribe from ports.
     *
     * 1. Stop data collection from ports
     * 2. Remove ports from Robot Sensor Data Object.
     * 3. Restart Data Collection Thread.
     *
     * @param json string from BPjs messages
     */
    private void unsubscribe(String json){
        System.out.println("in unsubscribe!");
        robotSensorsData.removeFromBoardsMap(json);
        startExecutor();
    }

    /**
     * Build IBoards according to json data from BPjs Build event.
     * @param json instructions on which IBoards to build.
     */
    private void build(String json) {
//        try {
//            robot = Robot.JsonToRobot(json);
//        } catch (Exception e){
//            e.printStackTrace();
//        }
        Map<Integer, IBoard> ev3 = Map.of(1, new FakeBoard(), 2, new FakeBoard());
        Map<Integer, IBoard> grovePi = Map.of(1, new FakeBoard(), 2, new FakeBoard());
        robot = new HashMap<>();
        robot.put(BoardTypeEnum.EV3, ev3);
        robot.put(BoardTypeEnum.GrovePi, grovePi);

        if (dataCollectionFuture != null){
            dataCollectionFuture.cancel(true);
        }
        robotSensorsData.clear();
        System.out.println("building success!");

    }

    /**
     * Call IBoard's 'drive' method according to json data
     * @param json info on boards, ports and values to call 'drive' on.
     */
    private void drive(String json){
        System.out.println("In drive");

        try {
            if (robot == null){
                return;
            }
            Map<BoardTypeEnum, Map<Integer, Map<IPortEnums, Double>>> activationMap = buildActivationMap(json);

            activationMap.forEach((boardName, indexesMap) -> {
                Map<Integer, IBoard> boardsMap = robot.get(boardName);

                activationMap.get(boardName).forEach((index, portsMap) -> {
                    @SuppressWarnings("unchecked")
                    IBoard<IPortEnums> board = boardsMap.get(index);
                    ArrayList<DriveDataObject> driveList = getDriveList(activationMap, boardName, index);
                    board.drive(driveList);

                    try {
                        Thread.sleep(commandTimeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Call IBoard's 'rotate' method according to json data
     *
     * @param json info on boards, ports and values to call 'rotate' on.
     */
    private void rotate(String json) {
        try {
            if (robot == null) {
                return;
            }
            Map<BoardTypeEnum, Map<Integer, Map<IPortEnums, Double>>> activationMap = buildActivationMap(json);

            activationMap.forEach((boardName, indexesMap) -> {
                Map<Integer, IBoard> boardsMap = robot.get(boardName);

                activationMap.get(boardName).forEach((index, portsMap) -> {
                    @SuppressWarnings("unchecked")
                    IBoard<IPortEnums> board = boardsMap.get(index);
                    ArrayList<DriveDataObject> driveList = getDriveList(activationMap, boardName, index);
                    board.rotate(driveList);

                    try {
                        Thread.sleep(commandTimeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            });
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Call IBoard's 'setSensorData' method according to json data
     *
     * @param json info on boards, ports and values to call 'setSensorData' on.
     */
    private void setSensor(String json) {
        try {
            if (robot == null) {
                return;
            }
            Map<BoardTypeEnum, Map<Integer, Map<IPortEnums, Double>>> activationMap = buildActivationMap(json);

            activationMap.forEach((boardName, indexesMap) -> {
                Map<Integer, IBoard> boardsMap = robot.get(boardName);

                activationMap.get(boardName).forEach((index, portsMap) -> {
                    @SuppressWarnings("unchecked")
                    IBoard<IPortEnums> board = boardsMap.get(index - 1);

                    Map<IPortEnums, Double> sensorsDataMap = activationMap.get(boardName).get(index);
                    sensorsDataMap.forEach((port, sensorValue) -> board.setSensorData(port, sensorValue > 0));
                    sensorsDataMap.forEach((port, sensorValue) -> System.out.println("Sensor in port " + port + " was set to value " + (sensorValue > 0)));

                    try {
                        Thread.sleep(commandTimeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Build Map of Board Types -> IBoard Index -> Port and it's speed value.
     * This map is used to call 'drive' on each IBoard that is indexed on the result
     * @param json build map according to this json
     * @return Map with boards, their indexes, and the data to call 'drive' on.
     */
    private Map<BoardTypeEnum, Map<Integer, Map<IPortEnums, Double>>> buildActivationMap(String json){
        Map<BoardTypeEnum, Map<Integer, Map<IPortEnums, Double>>> result = new HashMap<>();
        Gson gson = new Gson();
        Map element = gson.fromJson(json, Map.class); // json String to Map

        for (Object key: element.keySet()){ // Iterate over board types
            BoardTypeEnum keyAsBoard = BoardTypeEnum.valueOf((String) key);
            result.put(keyAsBoard, new HashMap<>()); // Add board enum to map
            Object indexesMap = element.get(key);

            @SuppressWarnings("unchecked")
            Map<String, Object> valueMapped = (Map<String, Object>)indexesMap; // Map of boards to ports list

            // Check if board contains map of board indexes to map of ports and values
            // or if the map is immediately ports and values.
            // The latter case means treating it as ports that belong to the first board

            // We are going to check the mapping VALUE.
            // If its a map, then we have board indexing, else we have port and values.

            Optional anyValue = valueMapped.values().stream().findFirst();
            if (!anyValue.isPresent()){
                continue;
            }

            // Check if board indexes exist
            if (anyValue.get() instanceof LinkedTreeMap){
                // We know the rest of the structure from here
                @SuppressWarnings("unchecked")
                Map<String, Map<String, Double>> boardIndexes = (Map<String, Map<String, Double>>) indexesMap; // Map of boards to ports list

                boardIndexes.forEach((index, mapping) -> {
                    Integer boardIndexAsInt = Integer.parseInt(index);
                    result.get(keyAsBoard).put(boardIndexAsInt, new HashMap<>());

                    mapping.forEach((port, value) ->
                            result.get(keyAsBoard).get(boardIndexAsInt).put(keyAsBoard.getPortType(port), value));

                });

            } else if (anyValue.get() instanceof Double){
                @SuppressWarnings("unchecked")
                Map<String, Double> boardPorts = (Map<String, Double>) indexesMap; // Map of boards to ports list
                Map<IPortEnums, Double> portsResult = new HashMap<>();

                boardPorts.forEach((port, value) -> portsResult.put(keyAsBoard.getPortType(port), value));

                result.get(keyAsBoard).put(1, portsResult);
            }
        }
        return result;
    }

    // TODO 3 parameters are called on first line only. Why not pass speedMap instead?
    private ArrayList<DriveDataObject> getDriveList(Map<BoardTypeEnum, Map<Integer, Map<IPortEnums, Double>>> activationMap, BoardTypeEnum boardName, int index) {
        Map<IPortEnums, Double> speedMap = activationMap.get(boardName).get(index);
        ArrayList<DriveDataObject> driveList = new ArrayList<>();
        speedMap.forEach((port, speed) -> driveList.add(new DriveDataObject(port, speed, 180)));
        speedMap.forEach((a, b) -> System.out.println(a + " - " + b));

        Map<String, Double> ev3 = robotSensorsData.getPortsAndValues("EV3", "_1");
        if (ev3 != null){
            System.out.println(ev3.get("_2"));
        }
        Map<String, Double> gp = robotSensorsData.getPortsAndValues("GrovePi", "_1");
        if (gp != null){
            System.out.println(gp.get("D4"));
        }
        return driveList;
    }


    /**
     * Create Runnable, which build json with all the subscribed ports and their connected sensors values.
     * This json is then used to update the sensor values inside RobotSensorsData.
     */
    private Runnable dataCollector = () -> {

        try {
            RobotSensorsData robotSensorsDataCopy = robotSensorsData.clone();

            JsonObject jsonBoards = new JsonObject();
            robotSensorsDataCopy.getBoardNames().forEach(boardString ->{
                JsonObject jsonIndexes = new JsonObject();
                BoardTypeEnum board = BoardTypeEnum.valueOf(boardString);
                robotSensorsDataCopy.getBoardIndexes(boardString).forEach(indexString -> {
                    JsonObject jsonPorts = new JsonObject();
                    int index = Integer.parseInt(indexString.substring(1));
                    robotSensorsDataCopy.getPorts(boardString, indexString).forEach(portString -> {
                        IPortEnums port = board.getPortType(portString);
                        Double data = robot.get(board).get(index).getDoubleSensorData(port, 0);
                        jsonPorts.addProperty(portString, data);
                    });
                    jsonIndexes.add(indexString, jsonPorts);
                });
                jsonBoards.add(boardString, jsonIndexes);
            });
            robotSensorsData.updateBoardMapValues(jsonBoards.toString());
        }   catch (Exception e){
            e.printStackTrace();
//            robot.forEach((name, v) -> {
//                System.out.println("name - ");
//                v.forEach((i, b) -> {
//                    System.out.println("    " + i + " - " + b.getClass().getName());
//                });
//            });
        }
    };

    private void startExecutor(){
        if (dataCollectionFuture != null){
            dataCollectionFuture.cancel(true);
        }

        try {
            dataCollectionFuture = executor.scheduleWithFixedDelay(dataCollector, 0L, 100L, TimeUnit.MILLISECONDS);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Uniform Interface for BPjs Commands
     */
    @FunctionalInterface
    public interface ICommand {
        void executeCommand(String json) throws IOException;
    }
}