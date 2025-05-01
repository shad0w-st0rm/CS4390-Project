// Worked on by Tanmaye (and modified by Ethan slightly)

import java.util.Stack;

/**
 * Utility class for solving mathematical equations.
 * Supports basic arithmetic operations (+, -, *, /) and parentheses.
 * Converts infix equations to postfix notation and evaluates them using a stack-based approach.
 */
public class MathSolver {

    /**
     * Inner class to represent the result of a mathematical operation.
     * Includes the result value, the original equation, a status code, and a status message.
     */
    public static class MathResult {
        // Status codes for the result
        public static final int SUCCESS = 0; // Operation was successful
        public static final int PAREN_ERROR = 1; // Mismatched parentheses
        public static final int DIV_ZERO = 2; // Division by zero
        public static final int INVALID_FORMAT = 3; // Invalid equation format
        public static final int PARSE_ERROR = 4; // Unable to parse the equation

        private float value; // The result of the calculation
        private String equation; // The original equation
        private int statusCode; // The status code of the operation
        private String statusMsg; // A descriptive message about the status

        /**
         * Constructor to initialize the MathResult object.
         * @param value The result of the calculation.
         * @param equation The original equation.
         * @param statusCode The status code of the operation.
         * @param statusMsg A descriptive message about the status.
         */
        public MathResult(float value, String equation, int statusCode, String statusMsg) {
            this.value = value;
            this.equation = equation;
            this.statusCode = statusCode;
            this.statusMsg = statusMsg;
        }

        // Getters for the MathResult fields
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

    /**
     * Solves a mathematical equation provided as a string.
     * @param equation The equation to solve.
     * @return A MathResult object containing the result or an error message.
     */
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

            // Return the successful result
            return new MathResult(result, equation, MathResult.SUCCESS, "Success");
        } catch (ArithmeticException e) {
            // Handle division by zero
            return new MathResult(0, equation, MathResult.DIV_ZERO, "Error: Division by zero");
        } catch (Exception e) {
            // Handle general parsing errors
            return new MathResult(0, equation, MathResult.PARSE_ERROR, "Error: Unable to parse equation");
        }
    }

    /**
     * Validates if the parentheses in the equation are balanced.
     * @param equation The equation to validate.
     * @return True if parentheses are balanced, false otherwise.
     */
    private static boolean areParenthesesBalanced(String equation) {
        Stack<Character> stack = new Stack<>();
        for (char c : equation.toCharArray()) {
            if (c == '(') {
                stack.push(c); // Push opening parentheses onto the stack
            } else if (c == ')') {
                if (stack.isEmpty() || stack.pop() != '(') {
                    return false; // Mismatched or unbalanced parentheses
                }
            }
        }
        return stack.isEmpty(); // Ensure no unmatched opening parentheses remain
    }

    /**
     * Converts an infix equation to postfix notation (Reverse Polish Notation).
     * @param equation The infix equation to convert.
     * @return The postfix representation of the equation.
     */
    private static String infixToPostfix(String equation) {
        StringBuilder postfix = new StringBuilder();
        Stack<Character> operators = new Stack<>();
        for (int i = 0; i < equation.length(); i++) {
            char c = equation.charAt(i);

            // If the character is a digit or a decimal point, append it to the postfix expression
            if (Character.isDigit(c) || c == '.') {
                postfix.append(c);
            } else if (c == '(') {
                operators.push(c); // Push opening parentheses onto the stack
            } else if (c == ')') {
                // Pop operators until an opening parenthesis is encountered
                while (!operators.isEmpty() && operators.peek() != '(') {
                    postfix.append(' ').append(operators.pop());
                }
                operators.pop(); // Remove the opening parenthesis
            } else if (isOperator(c)) {
                postfix.append(' '); // Add space to separate numbers
                // Pop operators with higher or equal precedence
                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(c)) {
                    postfix.append(operators.pop()).append(' ');
                }
                operators.push(c); // Push the current operator onto the stack
            }
        }

        // Pop all remaining operators from the stack
        while (!operators.isEmpty()) {
            postfix.append(' ').append(operators.pop());
        }

        return postfix.toString();
    }

    /**
     * Evaluates a postfix expression.
     * @param postfix The postfix expression to evaluate.
     * @return The result of the evaluation.
     */
    private static float evaluatePostfix(String postfix) {
        Stack<Float> stack = new Stack<>();
        String[] tokens = postfix.split("\\s+"); // Split the postfix expression into tokens

        for (String token : tokens) {
            if (token.isEmpty()) {
                continue; // Skip empty tokens
            }
            if (isNumber(token)) {
                stack.push(Float.parseFloat(token)); // Push numbers onto the stack
            } else if (isOperator(token.charAt(0))) {
                // Pop two operands from the stack and apply the operator
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

        return stack.pop(); // The final result is the last value on the stack
    }

    /**
     * Checks if a character is a valid operator.
     * @param c The character to check.
     * @return True if the character is an operator, false otherwise.
     */
    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    /**
     * Determines the precedence of an operator.
     * Higher values indicate higher precedence.
     * @param operator The operator to check.
     * @return The precedence of the operator.
     */
    private static int precedence(char operator) {
        switch (operator) {
            case '+':
            case '-':
                return 1; // Addition and subtraction have the lowest precedence
            case '*':
            case '/':
                return 2; // Multiplication and division have higher precedence
            default:
                return -1; // Invalid operator
        }
    }

    /**
     * Checks if a string represents a valid number.
     * @param token The string to check.
     * @return True if the string is a valid number, false otherwise.
     */
    private static boolean isNumber(String token) {
        try {
            Float.parseFloat(token); // Attempt to parse the string as a float
            return true;
        } catch (NumberFormatException e) {
            return false; // The string is not a valid number
        }
    }
}
