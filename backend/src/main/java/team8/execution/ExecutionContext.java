package team8.execution;

import lombok.Getter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ExecutionContext {
    private final Map<String, Object> variables = new HashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final String sessionId;
    private boolean stopped = false;

    public ExecutionContext(SimpMessagingTemplate messagingTemplate, String sessionId) {
        this.messagingTemplate = messagingTemplate;
        this.sessionId = sessionId;
    }

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public Object getVariable(String name) {
        return variables.get(name);
    }

    public void sendOutput(String type, Object data) {
        if (messagingTemplate != null && sessionId != null) {
            OutputMessage message = new OutputMessage(type, data);
            messagingTemplate.convertAndSend("/topic/execution/" + sessionId, message);
        }
    }

    public void stop() {
        this.stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }

    public Object evaluateExpression(String expression) {
        if (expression == null || expression.isEmpty()) {
            return null;
        }

        expression = expression.trim();

        if (expression.isEmpty()) {
            return null;
        }

        if (expression.startsWith("\"") && expression.endsWith("\"")) {
            return expression.substring(1, expression.length() - 1);
        }

        try {
            return Double.parseDouble(expression);
        } catch (NumberFormatException e) {
            Object value = variables.get(expression);
            if (value != null) {
                return value;
            }
            return expression;
        }
    }

    public boolean evaluateCondition(String condition) {
        if (condition == null || condition.isEmpty()) {
            return false;
        }

        condition = condition.trim();

        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            Object left = evaluateExpression(parts[0].trim());
            Object right = evaluateExpression(parts[1].trim());

            if (left == null || right == null) {
                return false;
            }

            if (left instanceof Number && right instanceof Number) {
                return toDouble(left) == toDouble(right);
            }

            return left.toString().equals(right.toString());
        } else if (condition.contains(">")) {
            String[] parts = condition.split(">");
            double left = toDouble(evaluateExpression(parts[0].trim()));
            double right = toDouble(evaluateExpression(parts[1].trim()));
            return left > right;
        } else if (condition.contains("<")) {
            String[] parts = condition.split("<");
            double left = toDouble(evaluateExpression(parts[0].trim()));
            double right = toDouble(evaluateExpression(parts[1].trim()));
            return left < right;
        }

        return false;
    }

    private double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
