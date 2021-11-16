package code.searchproblem.general;

public abstract class State {
    
    public abstract State clone();
    
    public abstract String encode();

    public abstract String hash();

    public abstract boolean isValidOperator(Operator operator);

    public abstract void updateState(Operator operator);
    
}
