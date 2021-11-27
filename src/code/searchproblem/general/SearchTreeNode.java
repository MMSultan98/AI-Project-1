package code.searchproblem.general;

public class SearchTreeNode {
    
    public String state;
    public SearchTreeNode parent;
    public Operator operator;
    public int depth;
    public long pathCost;
    public long fValue;

    // Constructor For Root Node Creation
    public SearchTreeNode(String state) {
        this.state = state;
        this.parent = null;
        this.operator = null;
        this.depth = 0;
        this.pathCost = 0;
    }
    
    // Constructor For Other Nodes
    public SearchTreeNode(String state, SearchTreeNode parent, Operator operator) {
        this.state = state;
        this.parent = parent;
        this.operator = operator;
        this.depth = parent.depth + 1;
    }
}

