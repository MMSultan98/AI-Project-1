package code.generalsearchproblem;

import java.util.ArrayList;

public abstract class SearchProblem {

    public ArrayList<Operator> operators;
    public State initialState;
    
    // getNextState function is used to generate the state space
    public abstract String getNextState(State state, Operator operator);
    
    public abstract boolean goalTest(State state);
    
    public abstract int pathCost(SearchTreeNode node);
    
}
