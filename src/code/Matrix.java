package code;

import java.util.ArrayList;
import java.util.LinkedList;
import code.searchproblem.general.*;
import code.searchproblem.strategies.SearchStrategy;
import code.matrix.evaluationfunctions.*;
import code.matrix.general.MatrixOperator;
import code.matrix.general.MatrixState;
import code.matrix.helpers.*;
import code.matrix.objects.*;

public class Matrix extends SearchProblem {

    private int maximumNumberOfSteps;

    public Matrix(String initialGrid) {
        // Create and populate the array of opeators
        this.operators = new ArrayList<Operator>(9);
        this.operators.add(MatrixOperator.UP);
        this.operators.add(MatrixOperator.DOWN);
        this.operators.add(MatrixOperator.LEFT);
        this.operators.add(MatrixOperator.RIGHT);
        this.operators.add(MatrixOperator.CARRY);
        this.operators.add(MatrixOperator.DROP);
        this.operators.add(MatrixOperator.TAKEPILL);
        this.operators.add(MatrixOperator.FLY);
        this.operators.add(MatrixOperator.KILL);
        
        // Split the state info from the genGrid output
        String[] splitState = initialGrid.split(";");
        
        // Initialize neo damage to 0
        splitState[2] += ",0";
        
        // Initialize agent isKilled to false
        String[] splitAgentsInfo = splitState[4].split(",");
        for (int i = 1; i < splitAgentsInfo.length; i+=2) {
            splitAgentsInfo[i] += ",f";
        }
        splitState[4] = String.join(",", splitAgentsInfo);

        // Initialize pills isTaken to false
        String[] splitPillsInfo = splitState[5].split(",");
        for (int i = 1; i < splitPillsInfo.length; i+=2) {
            splitPillsInfo[i] += ",f";
        }
        splitState[5] = String.join(",", splitPillsInfo);
        
        // Initialize hostages isAgent, isKilled, and isCarried to false
        String[] splitHostagesInfo = splitState[7].split(",");
        for (int i = 2; i < splitHostagesInfo.length; i+=3) {
            splitHostagesInfo[i] += ",f,f,f";
        }
        splitState[7] = String.join(",", splitHostagesInfo);

        // Rejoin the state info after adding the extra info 
        String initialStateString = String.join(";", splitState);

        // Create the initial state object
        this.initialState = new MatrixState(initialStateString);

        // Set the name of the state class
        this.stateClassName = this.initialState.getClass().getName();
        
        // Initialize the number of expanded nodes
        this.expandedNodes = 0;

        // Set the maximum number of steps for this matrix problem
        this.maximumNumberOfSteps = ((MatrixState) this.initialState).getMaximumNumberOfSteps();
    }
    
    @Override
    public State getNextState(State state, Operator operator) {
        if (state.isValidOperator(operator)) {
            State nextState = state.clone();
            nextState.updateState(operator);
            return nextState;
        }
         return null;
    }

    @Override
    public boolean goalTest(State state) {
        MatrixState matrixState = (MatrixState) state;
        if (!matrixState.neo.position.equals(matrixState.telephoneBooth.position)) {
            return false;
        }
        for (Hostage hostage : matrixState.hostages) {
            if (!hostage.isDisappeared(matrixState.telephoneBooth)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public long pathCost(SearchTreeNode node) {
        MatrixState state = new MatrixState(node.state);
        long base = this.maximumNumberOfSteps + 1;
        long numberOfSteps = node.depth;
        long numberOfKills = state.getKills();
        long numberOfDeaths = state.getDeaths();
        return numberOfSteps + (numberOfKills * base) + (numberOfDeaths * base * base);
    }

    public boolean checkSolution(String solution, boolean visualize) {
        String[] splitSolution = solution.split(";");
        String[] plan = splitSolution[0].split(",");
        int numberOfDeaths = Integer.parseInt(splitSolution[1]);
        int numberOfKills = Integer.parseInt(splitSolution[2]);
        boolean isValidSolution = true;
        
        MatrixState currentState = (MatrixState) this.initialState;
        String visualizedSolution = "\n" + "Initial state" + "\n\n";
        visualizedSolution += currentState;
        visualizedSolution += "\n" + "_".repeat(15 * (currentState.m+1)) + "\n\n";
        for (String operator : plan) {
            currentState = (MatrixState) getNextState(currentState, MatrixOperator.getOperator(operator));
            if (currentState == null) {
                isValidSolution = false;
                visualizedSolution += "Invalid operator: " + operator;
                break;
            }
            else {
                visualizedSolution += "Operator: " + operator + "\n\n";
                visualizedSolution += currentState;
                visualizedSolution += "\n" + "_".repeat(15 * (currentState.m+1)) + "\n\n";
            }
        }
        
        if (isValidSolution) {
            if (!this.goalTest(currentState)) {
                isValidSolution = false;
                visualizedSolution += "The final state is not a goal state";
            }
            else if (currentState.getDeaths() != numberOfDeaths) {
                isValidSolution = false;
                visualizedSolution += "The number of deaths is not correct";
            }
            else if (currentState.getAgentKills() != numberOfKills) {
                isValidSolution = false;
                visualizedSolution += "The number of kills is not correct";
            }
            else {
                visualizedSolution += "The solution is valid";
            }
        }

        if (visualize) {
            visualizedSolution += "\n";
            System.out.println(visualizedSolution);
        }
        return isValidSolution;
    }

    public static String genGrid() {
        // Generate grid size
        int m = HelperMethods.genrateRandomInt(5, 16);
        int n = HelperMethods.genrateRandomInt(5, 16);
    
        // Generate number of hostages, pills, pads and agents
        int numberOfAvailablePositions = m * n;
        numberOfAvailablePositions -= 2;
        int numberOfHostages = HelperMethods.genrateRandomInt(3, 11);
        numberOfAvailablePositions -= numberOfHostages;
        int numberOfPills = HelperMethods.genrateRandomInt(1, numberOfHostages+1);
        numberOfAvailablePositions -= numberOfPills;
        int numberOfPads = HelperMethods.genrateRandomInt(1, (int) Math.ceil(numberOfAvailablePositions/2.0));
        numberOfAvailablePositions -= numberOfPads * 2;
        int numberOfAgents = HelperMethods.genrateRandomInt(1, numberOfAvailablePositions+1);
        numberOfAvailablePositions -= numberOfAgents;
    
        // Generate maximum number of hostages that can be carried
        int c = HelperMethods.genrateRandomInt(1, 5);
    
        // Populate grid
        ArrayList<Position> taken = new ArrayList<Position>();
    
        Position neoPosition = Position.genrateRandomPosition(m, n, taken);
        taken.add(neoPosition);
    
        Position telephoneBoothPosition = Position.genrateRandomPosition(m, n, taken);
        taken.add(telephoneBoothPosition);
    
        ArrayList<Position> hostagesPositions = new ArrayList<Position>();
        ArrayList<Integer> hostagesDamages = new ArrayList<Integer>();
        for (int i = 0; i < numberOfHostages; i++) {
            Position hostagePosition = Position.genrateRandomPosition(m, n, taken);
            hostagesPositions.add(hostagePosition);
            taken.add(hostagePosition);
            hostagesDamages.add(HelperMethods.genrateRandomInt(1, 100));
        }
    
        ArrayList<Position> pillsPositions = new ArrayList<Position>();
        for (int i = 0; i < numberOfPills; i++) {
            Position pillPosition = Position.genrateRandomPosition(m, n, taken);
            pillsPositions.add(pillPosition);
            taken.add(pillPosition);
        }
    
        ArrayList<Position> startPadsPositions = new ArrayList<Position>();
        ArrayList<Position> finishPadsPositions = new ArrayList<Position>();
        for (int i = 0; i < numberOfPads; i++) {
            Position startPadPosition = Position.genrateRandomPosition(m, n, taken);
            taken.add(startPadPosition);
            startPadsPositions.add(startPadPosition);
            Position finishPadPosition = Position.genrateRandomPosition(m, n, taken);
            taken.add(finishPadPosition);
            finishPadsPositions.add(finishPadPosition);
        }
    
        ArrayList<Position> agentsPositions = new ArrayList<Position>();
        for (int i = 0; i < numberOfAgents; i++) {
            Position agentPosition = Position.genrateRandomPosition(m, n, taken);
            agentsPositions.add(agentPosition);
            taken.add(agentPosition);
        }
    
        // Encoding the grid in a string
        String grid = "";
        grid += m + "," + n + ";" + c + ";";
        grid += neoPosition.x + "," + neoPosition.y + ";";
        grid += telephoneBoothPosition.x + "," + telephoneBoothPosition.y + ";";
        for (int i = 0; i < agentsPositions.size(); i++) {
            grid += agentsPositions.get(i).x + "," + agentsPositions.get(i).y;
            grid += i == agentsPositions.size()-1 ? ";" : ",";
        }
        for (int i = 0; i < pillsPositions.size(); i++) {
            grid += pillsPositions.get(i).x + "," + pillsPositions.get(i).y;
            grid += i == pillsPositions.size()-1 ? ";" : ",";
        }
        for (int i = 0; i < startPadsPositions.size(); i++) {
            grid += startPadsPositions.get(i).x + "," + startPadsPositions.get(i).y + ",";
            grid += finishPadsPositions.get(i).x + "," + finishPadsPositions.get(i).y + ",";
            grid += finishPadsPositions.get(i).x + "," + finishPadsPositions.get(i).y + ",";
            grid += startPadsPositions.get(i).x + "," + startPadsPositions.get(i).y;
            grid += i == startPadsPositions.size()-1 ? ";" : ",";
        }
        for (int i = 0; i < hostagesPositions.size(); i++) {
            grid += hostagesPositions.get(i).x + "," + hostagesPositions.get(i).y + ",";
            grid += hostagesDamages.get(i);
            grid += i == hostagesPositions.size()-1 ? "" : ",";
        }
    
        return grid;
    }

    public static String solve(String initialGrid, String strategy, boolean visualize) {
        Matrix matrix = new Matrix(initialGrid);
        
        SearchTreeNode goalNode = null;
        switch (strategy) {
            case "BF": {
                goalNode = SearchStrategy.breadthFirstSearch(matrix);
                break;
            }
            case "DF": {
                goalNode = SearchStrategy.depthFirstSearch(matrix);
                break;
            }
            case "ID": {
                goalNode = SearchStrategy.iterativeDeepeningSearch(matrix);
                break;
            }
            case "UC": {
                goalNode = SearchStrategy.uniformCostSearch(matrix);
                break;
            }
            case "GR1": {
                goalNode = SearchStrategy.bestFirstSearch(matrix, new MatrixGreedy1());
                break;
            }
            case "GR2": {
                goalNode = SearchStrategy.bestFirstSearch(matrix, new MatrixGreedy2());
                break;
            }
            case "AS1": {
                goalNode = SearchStrategy.bestFirstSearch(matrix, new MatrixAStar1());
                break;
            }
            case "AS2": {
                goalNode = SearchStrategy.bestFirstSearch(matrix, new MatrixAStar2());
                break;
            }
            default:
                break;
        }
        
        if (goalNode == null) {
            if (visualize) {
                System.out.println("No Solution");
                System.out.println("Number of expanded nodes: " + matrix.expandedNodes);
            }
            return "No Solution";
        }
        
        SearchTreeNode currentNode = goalNode;
        String plan = "";
        LinkedList<String> gridVisualize = new LinkedList<String>();
        while (currentNode != null) {
            String currentOperator = currentNode.operator != null ? currentNode.operator.toString().toLowerCase() : "";
            if (currentOperator.equals("takepill")) {
                currentOperator = "takePill";
            }
            plan = currentOperator + plan;
            plan = ((currentNode.parent != null && currentNode.parent.parent != null) ? "," : "") + plan;
            if (visualize) {
                MatrixState currentState = new MatrixState(currentNode.state);
                String grid = currentOperator != "" ? "Operator: " + currentOperator + "\n\n" : "Initial state" + "\n\n";
                grid += currentState;
                gridVisualize.addFirst(grid);
            }
            currentNode = currentNode.parent;
        }
        MatrixState goalState = new MatrixState(goalNode.state);
        String solution = plan + ";" + goalState.getDeaths() + ";" + goalState.getKills() + ";" + matrix.expandedNodes;

        if (visualize) {
            MatrixState initialState = (MatrixState) matrix.initialState;
            for (String grid : gridVisualize) {
                System.out.println("\n" + grid);
                System.out.println("_".repeat(15 * (initialState.m+1)));
            }
            System.out.println("\n" + "Solution: " + solution);
            System.out.println("Number of steps: " + plan.split(",").length + "\n");
        }

        return solution;
    }
    
}
