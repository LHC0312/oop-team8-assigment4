package team8.execution;

import lombok.Getter;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import team8.model.expression.ExpressionBlock;
import team8.model.expression.LiteralExpressionBlock;
import team8.model.expression.VariableExpressionBlock;
import team8.repository.ExpressionRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class ExecutionContext {
    private final Map<Long, Object> variablesById = new HashMap<>();
    private final Map<Long, String> variableTypesById = new HashMap<>();
    private final Map<Long, String> variableNamesById = new HashMap<>();
    private final Map<String, Long> variableIdByName = new HashMap<>();
    private long variableSeq = 1L;
    private final SimpMessagingTemplate messagingTemplate;
    private final String sessionId;
    private final boolean debugMode;
    private final boolean traceEnabled;
    private final ExpressionRepository expressionRepository;
    private final Map<Long, ExpressionBlock> expressionCache = new HashMap<>();
    private boolean stopped = false;

    public ExecutionContext(SimpMessagingTemplate messagingTemplate, String sessionId) {
        this(messagingTemplate, sessionId, false, false, null);
    }

    public ExecutionContext(SimpMessagingTemplate messagingTemplate, String sessionId, boolean debugMode, boolean traceEnabled) {
        this(messagingTemplate, sessionId, debugMode, traceEnabled, null);
    }

    public ExecutionContext(SimpMessagingTemplate messagingTemplate, String sessionId,
                            boolean debugMode, boolean traceEnabled,
                            ExpressionRepository expressionRepository) {
        this.messagingTemplate = messagingTemplate;
        this.sessionId = sessionId;
        this.debugMode = debugMode;
        this.traceEnabled = traceEnabled;
        this.expressionRepository = expressionRepository;
    }

    public void setVariable(Long variableId, Object value) {
        variablesById.put(variableId, value);
    }

    /**
     * 기존 이름 기반 코드 호환용. 이름이 등록되어 있지 않으면 내부적으로 ID를 하나 생성해 등록합니다.
     */
    public void setVariable(String name, Object value) {
        Long id = variableIdByName.computeIfAbsent(name, n -> variableSeq++);
        variableNamesById.putIfAbsent(id, name);
        variablesById.put(id, value);
    }

    public Object getVariable(Long variableId) {
        return variablesById.get(variableId);
    }

    /**
     * 기존 이름 기반 코드 호환용. 등록된 이름이 없으면 null을 반환합니다.
     */
    public Object getVariable(String name) {
        Long id = variableIdByName.get(name);
        return id == null ? null : variablesById.get(id);
    }

    public Map<String, Object> getVariablesSnapshot() {
        Map<String, Object> res = new HashMap<>();
        for (Map.Entry<Long, Object> e : variablesById.entrySet()) {
            String name = variableNamesById.getOrDefault(e.getKey(), "var_" + e.getKey());
            res.put(name, e.getValue());
        }
        return res;
    }

    public void setVariableMeta(Long id, String name, String type) {
        variableNamesById.put(id, name);
        variableTypesById.put(id, type);
        if (name != null) {
            variableIdByName.put(name, id);
        }
    }

    /**
     * 기존 이름 기반 코드 호환용 타입 등록.
     */
    public void setVariableType(String name, String type) {
        Long id = variableIdByName.computeIfAbsent(name, n -> variableSeq++);
        variableNamesById.putIfAbsent(id, name);
        variableTypesById.put(id, type);
    }

    public String getVariableType(Long id) {
        return variableTypesById.get(id);
    }

    public void sendOutput(String type, Object data) {
        if (messagingTemplate != null && sessionId != null) {
            OutputMessage message = new OutputMessage(type, data);
            messagingTemplate.convertAndSend("/topic/execution/" + sessionId, message);
        }
    }

    public void sendDebug(String type, Object data) {
        if (debugMode) {
            sendOutput(type, data);
        }
    }

    public void sendTrace(String type, Object data) {
        if (traceEnabled) {
            sendOutput(type, data);
        }
    }

    public void stop() {
        this.stopped = true;
    }

    public boolean isStopped() {
        return stopped;
    }

    public Object evaluateExpression(ExpressionBlock expressionBlock) {
        if (expressionBlock == null) {
            return null;
        }
        if (expressionBlock instanceof HibernateProxy proxy) {
            expressionBlock = unwrapProxy(proxy);
        }
        cacheExpression(expressionBlock);
        return expressionBlock.evaluate(this);
    }

    public boolean evaluateCondition(ExpressionBlock expressionBlock) {
        Object result = evaluateExpression(expressionBlock);
        if (result == null) {
            return false;
        }
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        return toBoolean(result);
    }

    /**
     * 표현식 블록을 문자열로 평가한다.
     * PRINT 등 출력용으로 사용하며, 값이 null이면 빈 문자열을 반환하고
     * '+' 연산은 문자열 연결로 처리한다.
     */
    public String evaluateExpressionAsString(ExpressionBlock expressionBlock) {
        if (expressionBlock == null) {
            return "";
        }
        if (expressionBlock instanceof HibernateProxy proxy) {
            expressionBlock = unwrapProxy(proxy);
        }
        cacheExpression(expressionBlock);
        return expressionBlock.evaluateAsString(this);
    }

    private ExpressionBlock unwrapProxy(HibernateProxy proxy) {
        var initializer = proxy.getHibernateLazyInitializer();
        if (!initializer.isUninitialized()) {
            return (ExpressionBlock) initializer.getImplementation();
        }
        Object identifier = initializer.getIdentifier();
        if (identifier instanceof Long id && expressionRepository != null) {
            return loadExpressionById(id);
        }
        return (ExpressionBlock) initializer.getImplementation();
    }

    public Object parseLiteral(LiteralExpressionBlock literal) {
        if (literal == null) {
            return null;
        }
        String type = literal.getLiteralType();
        String value = literal.getValue();
        if ("BOOLEAN".equalsIgnoreCase(type)) {
            return Boolean.parseBoolean(value);
        }
        if ("NUMBER".equalsIgnoreCase(type)) {
            return Double.parseDouble(value);
        }
        if ("STRING".equalsIgnoreCase(type) || type == null) {
            // type이 없으면 문자열로 간주
            if (value == null) {
                return "";
            }
            // 숫자/불리언 문자열을 그대로 해석하지 않고 문자열 그대로 사용
            return value;
        }
        return value;
    }

    public Object applyUnary(String operator, Object operand) {
        return switch (operator) {
            case "-" -> -toDouble(operand, true);
            case "!" -> !toBoolean(operand);
            default -> operand;
        };
    }

    public Object applyBinary(String operator, Object left, Object right) {
        return switch (operator) {
            case "+" -> add(left, right);
            case "-" -> subtract(left, right);
            case "*" -> multiply(left, right);
            case "/" -> divide(left, right);
            case "%" -> mod(left, right);
            case "==" -> equalsStrict(left, right);
            case "!=" -> !equalsStrict(left, right);
            case ">" -> compareNumeric(left, right) > 0;
            case "<" -> compareNumeric(left, right) < 0;
            case ">=" -> compareNumeric(left, right) >= 0;
            case "<=" -> compareNumeric(left, right) <= 0;
            case "&&" -> toBoolean(left) && toBoolean(right);
            case "||" -> toBoolean(left) || toBoolean(right);
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
    }

    private boolean equalsStrict(Object left, Object right) {
        if (left == null || right == null) {
            return left == right;
        }
        if (left instanceof Number && right instanceof Number) {
            return Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue()) == 0;
        }
        if (left.getClass() != right.getClass()) {
            throw new IllegalArgumentException("Cannot compare different types: " + left.getClass().getSimpleName() + " vs " + right.getClass().getSimpleName());
        }
        return left.equals(right);
    }

    private double toDouble(Object value, boolean failOnNonNumeric) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (failOnNonNumeric) {
            throw new IllegalArgumentException("Numeric value required but was: " + value);
        }
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            if (failOnNonNumeric) {
                throw new IllegalArgumentException("Numeric value required but was: " + value);
            }
            return 0.0;
        }
    }

    private int compareNumeric(Object left, Object right) {
        double l = toDouble(left, true);
        double r = toDouble(right, true);
        return Double.compare(l, r);
    }

    private boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0.0;
        }
        if (value == null) {
            return false;
        }
        String text = value.toString().toLowerCase();
        if ("true".equals(text)) {
            return true;
        }
        if ("false".equals(text)) {
            return false;
        }
        throw new IllegalArgumentException("Boolean value required but was: " + value);
    }

    private Object add(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            return toDouble(left, true) + toDouble(right, true);
        }
        if (left instanceof String && right instanceof String) {
            return ((String) left) + right;
        }
        throw new IllegalArgumentException("Unsupported addition between " + typeName(left) + " and " + typeName(right));
    }

    private Object subtract(Object left, Object right) {
        return toDouble(left, true) - toDouble(right, true);
    }

    private Object multiply(Object left, Object right) {
        return toDouble(left, true) * toDouble(right, true);
    }

    private Object divide(Object left, Object right) {
        double divisor = toDouble(right, true);
        if (divisor == 0.0) {
            throw new IllegalArgumentException("Division by zero");
        }
        return toDouble(left, true) / divisor;
    }

    private Object mod(Object left, Object right) {
        double divisor = toDouble(right, true);
        if (divisor == 0.0) {
            throw new IllegalArgumentException("Modulo by zero");
        }
        return toDouble(left, true) % divisor;
    }

    private String typeName(Object value) {
        return value == null ? "null" : value.getClass().getSimpleName();
    }

    public Object resolveVariableValue(VariableExpressionBlock variable) {
        if (variable.getVariableId() != null) {
            if (!variablesById.containsKey(variable.getVariableId())) {
                Map<String, Object> trace = new HashMap<>();
                trace.put("reason", "variable_value_missing");
                trace.put("variableId", variable.getVariableId());
                if (variable.getVariableName() != null) {
                    trace.put("variableName", variable.getVariableName());
                }
                sendTrace("ERROR", trace);
                throw new IllegalStateException("Variable value not set for id: " + variable.getVariableId() +
                        (variable.getVariableName() != null ? " (" + variable.getVariableName() + ")" : ""));
            }
            return variablesById.get(variable.getVariableId());
        }
        if (variable.getVariableName() != null) {
            Long id = variableIdByName.get(variable.getVariableName());
            if (id == null) {
                Map<String, Object> trace = new HashMap<>();
                trace.put("reason", "unknown_variable_name");
                trace.put("variableName", variable.getVariableName());
                sendTrace("ERROR", trace);
                throw new IllegalStateException("Unknown variable name: " + variable.getVariableName());
            }
            if (!variablesById.containsKey(id)) {
                Map<String, Object> trace = new HashMap<>();
                trace.put("reason", "variable_value_missing");
                trace.put("variableId", id);
                trace.put("variableName", variable.getVariableName());
                sendTrace("ERROR", trace);
                throw new IllegalStateException("Variable value not set for name: " + variable.getVariableName());
            }
            return variablesById.get(id);
        }
        sendTrace("ERROR", Map.of(
                "reason", "variable_reference_missing_id_and_name"
        ));
        throw new IllegalStateException("Variable reference requires id or name");
    }

    public ExpressionBlock loadExpressionById(Long expressionId) {
        if (expressionId == null) {
            return null;
        }
        ExpressionBlock cached = expressionCache.get(expressionId);
        if (cached != null) {
            return cached;
        }
        if (expressionRepository == null) {
            throw new IllegalStateException("Expression repository is not configured but is required to resolve expression id: " + expressionId);
        }
        Optional<ExpressionBlock> found = expressionRepository.findById(expressionId);
        ExpressionBlock expressionBlock = found.orElseThrow(() -> new IllegalArgumentException("Expression not found: " + expressionId));
        cacheExpression(expressionBlock);
        return expressionBlock;
    }

    public void cacheExpression(ExpressionBlock expressionBlock) {
        if (expressionBlock != null && expressionBlock.getId() != null) {
            expressionCache.putIfAbsent(expressionBlock.getId(), expressionBlock);
        }
    }
}
