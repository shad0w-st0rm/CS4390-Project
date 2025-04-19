import java.util.Stack;

public class MathSolver {
    public static class MathResult {
        public static final int SUCCESS = 0;
        public static final int PAREN_ERROR = 1;
        public static final int DIV_ZERO = 2;
        public static final int INVALID_FORMAT = 3;
        public static final int PARSE_ERROR = 4;

        private float value;
        private String equation;
        private int statusCode;
        private String statusMsg;

        public MathResult(float value, String equation, int statusCode, String statusMsg) {
            this.value = value;
            this.equation = equation;
            this.statusCode = statusCode;
            this.statusMsg = statusMsg;
        }

        public float getValue() {
            return value;
        }

        public String getEquation() {
            return equation;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getStatusMessage() {
            return statusMsg;
        }

        @Override
        public String toString() {
            return "MathResult{" +
                    "value=" + value +
                    ", equation='" + equation + '\'' +
                    ", statusCode=" + statusCode +
                    ", statusMessage='" + statusMsg + '\'' +
                    '}';
        }
    }

    public static MathResult solveEquation(String equation) {
        try {
            // Remove spaces from the equation
            equation = equation.replaceAll("\\s+", "");

            // Validate parentheses
            if (!areParenthesesBalanced(equation)) {
                return new MathResult(0, equation, MathResult.PAREN_ERROR, "Error: Mismatched parentheses");
            }

            // Convert infix equation to postfix (Reverse Polish Notation)
            String postfix = infixToPostfix(equation);

            // Evaluate the postfix expression
            float result = evaluatePostfix(postfix);

            return new MathResult(result, equation, MathResult.SUCCESS, "Success");
        } catch (ArithmeticException e) {
            return new MathResult(0, equation, MathResult.DIV_ZERO, "Error: Division by zero");
        } catch (Exception e) {
            return new MathResult(0, equation, MathResult.PARSE_ERROR, "Error: Unable to parse equation");
        }
    }

    private static boolean areParenthesesBalanced(String equation) {
        Stack<Character> stack = new Stack<>();
        for (char c : equation.toCharArray()) {
            if (c == '(') {
                stack.push(c);
            } else if (c == ')') {
                if (stack.isEmpty() || stack.pop() != '(') {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }

    private static String infixToPostfix(String equation) {
        StringBuilder postfix = new StringBuilder();
        Stack<Character> operators = new Stack<>();
        for (int i = 0; i < equation.length(); i++) {
            char c = equation.charAt(i);

            // If the character is a digit or a decimal point, append it to the postfix expression
            if (Character.isDigit(c) || c == '.') {
                postfix.append(c);
            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    postfix.append(' ').append(operators.pop());
                }
                operators.pop(); // Remove '(' from the stack
            } else if (isOperator(c)) {
                postfix.append(' '); // Add space to separate numbers
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(c)) {
                    postfix.append(operators.pop()).append(' ');
                }
                operators.push(c);
            }
        }

        // Pop all remaining operators from the stack
        while (!operators.isEmpty()) {
            postfix.append(' ').append(operators.pop());
        }

        return postfix.toString();
    }

    private static float evaluatePostfix(String postfix) {
        Stack<Float> stack = new Stack<>();
        String[] tokens = postfix.split("\\s+");

        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            if (isNumber(token)) {
                stack.push(Float.parseFloat(token));
            } else if (isOperator(token.charAt(0))) {
                float b = stack.pop();
                float a = stack.pop();
                switch (token.charAt(0)) {
                    case '+':
                        stack.push(a + b);
                        break;
                    case '-':
                        stack.push(a - b);
                        break;
                    case '*':
                        stack.push(a * b);
                        break;
                    case '/':
                        if (b == 0) {
                            throw new ArithmeticException("Division by zero");
                        }
                        stack.push(a / b);
                        break;
                }
            }
        }

        return stack.pop();
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private static int precedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            default:
                return -1;
        }
    }

    private static boolean isNumber(String token) {
        try {
            Float.parseFloat(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
