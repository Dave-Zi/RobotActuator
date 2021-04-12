import Enums.BoardTypeEnum;
import Enums.IPortEnums;
import RobotData.RobotSensorsData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CommandHandler {

    private RobotSensorsData robotSensorsData;
    private HashMap<BoardTypeEnum, List<IBoard>> robot;

    private ICommand subscribe = this::subscribe;
    private ICommand unsubscribe = this::unsubscribe;
    private ICommand build = this::build;
    private ICommand drive = this::drive;

    private Map<String, ICommand> commandToMethod = Stream.of(new Object[][] {
            { "\"Subscribe\"",  subscribe},
            { "\"Unsubscribe\"", unsubscribe },
            { "\"Build\"", build },
            { "\"Drive\"", drive }
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (ICommand) data[1]));

    CommandHandler(RobotSensorsData robotSensorsData){
        this.robotSensorsData = robotSensorsData;
    }

    MessageContent parseCommand(String message){
        JsonObject obj = new JsonParser().parse(message).getAsJsonObject();
        String command = String.valueOf(obj.get("Command"));
        String dataJsonString = String.valueOf(obj.get("Data"));
        return new MessageContent(command, dataJsonString);
    }

    ICommand getCommand(String command){
        return commandToMethod.get(command);
    }

    private void subscribe(String json){
        System.out.println("in subscribe!");
        robotSensorsData.addToBoardsMap(json);
    }

    private void unsubscribe(String json){
        System.out.println("in unsubscribe!");
        robotSensorsData.removeFromBoardsMap(json);
    }

    private void build(String json) throws IOException {
        robot = Robot.JsonToRobot(json);
        System.out.println("building success!");

    }

    private void drive(String json){
        Map<BoardTypeEnum, Map<Integer, Map<IPortEnums, Double>>> activationMap = buildActivationMap(json);

        activationMap.forEach((boardName, indexesMap) -> {
            ArrayList<IBoard> boardsList = (ArrayList<IBoard>) robot.get(boardName);

            activationMap.get(boardName).forEach((index, portsMap) -> {
                @SuppressWarnings("unchecked")
                IBoard<IPortEnums> board = boardsList.get(index);
                Map<IPortEnums, Double> speedMap = activationMap.get(boardName).get(index);
                board.drive(speedMap);
            });
        });
    }

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



    @FunctionalInterface
    public interface ICommand {
        void executeCommand(String json) throws IOException;
    }
}

final class MessageContent {
    private final String command;
    private final String data;

    MessageContent(String command, String data) {
        this.command = command;
        this.data = data;
    }

    String getCommand() {
        return command;
    }

    String  getData() {
        return data;
    }
}
