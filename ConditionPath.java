import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ConditionPath {
    HashMap<String, String> variables; //variable to constant value

    HashMap<String, String> symbolsToVariable;

    public void setConditions(LinkedList<String> conditions) {
        this.conditions = (LinkedList<String>) conditions.clone();
    }

    LinkedList<String> conditions;

    public ConditionPath(HashMap<String, String> variables, HashMap<String, String> symbolsToVariable) {
        this.variables = (HashMap<String, String>) variables.clone();
        this.symbolsToVariable = (HashMap<String, String>) symbolsToVariable.clone();
        conditions = new LinkedList<>();
    }

}
