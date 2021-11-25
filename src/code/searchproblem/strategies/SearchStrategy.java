package code.searchproblem.strategies;

import java.lang.Class;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.LinkedList;
import code.searchproblem.general.*;
import code.searchproblem.strategies.queueingfunctions.*;

public class SearchStrategy {
    
    public static SearchTreeNode generalSearch(SearchProblem problem, QueueingFunction queueingFunction) {
        Class<?> stateClass;
        Constructor<?> constructor;
        try {
            stateClass = Class.forName(problem.stateClassName);
            constructor = stateClass.getConstructor(String.class);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        LinkedList<SearchTreeNode> queue = new LinkedList<SearchTreeNode>();
        HashSet<String> visitedStates = new HashSet<String>();
        queue.add(new SearchTreeNode(problem.initialState.stateString));
        visitedStates.add(problem.initialState.hash());
        while (!queue.isEmpty()) {
            problem.expandedNodes++;
            SearchTreeNode currentNode = queue.removeFirst();
            State currentState;
            try {
                currentState = (State) constructor.newInstance(currentNode.state);
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            
            if (problem.goalTest(currentState)) {
                return currentNode;
            }

            for (Operator operator : problem.operators) {
                State nextState = problem.getNextState(currentState, operator);
                if (nextState != null) {
                    if (visitedStates.add(nextState.hash())) {
                        SearchTreeNode newNode = new SearchTreeNode(nextState.stateString, currentNode, operator);
                        newNode.pathCost = problem.pathCost(newNode);
                        queueingFunction.enqueue(queue, newNode);
                    }
                }
            }
        }
        return null;
    }

    public static SearchTreeNode breadthFirstSearch(SearchProblem problem) {
        return generalSearch(problem, new InsertAtEnd());
    }
    
    public static SearchTreeNode depthFirstSearch(SearchProblem problem) {
        return generalSearch(problem, new InsertAtFront());
    }

    public static SearchTreeNode depthLimitedSearch(SearchProblem problem, int maximumDepth) {
        return generalSearch(problem, new InsertAtFront(maximumDepth));
    }

    public static SearchTreeNode iterativeDeepeningSearch(SearchProblem problem) {
        int i = 0;
        while (true) {
            SearchTreeNode result = depthLimitedSearch(problem, i);
            if (result != null)
                return result;
            i++;
        }
    }
    
    public static SearchTreeNode uniformCostSearch(SearchProblem problem) {
        return generalSearch(problem, new OrderedInsert());
    }

    public static SearchTreeNode bestFirstSearch(SearchProblem problem, EvaluationFunction evaluationFunction) {
        return generalSearch(problem, new OrderedInsert(evaluationFunction));
    }
    
}
