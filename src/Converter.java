public class Converter {
    private String[] operators = {"+", "-", "*", "/", "^"};
    private int[] priorities = {1, 1, 2, 2, 3};
    private String[] operands = new String[0];
    private boolean isCustom = false;

    public void setCustom(String[] ops, String[] customOps, int[] customPrio) {
        isCustom = true;
        operands = ops;
        operators = customOps;
        priorities = customPrio;
    }

    public void resetToDefault() {
        isCustom = false;
        operands = new String[0];
        operators = new String[]{"+", "-", "*", "/", "^"};
        priorities = new int[]{1, 1, 2, 2, 3};
    }

    public String infixToPostfix(String infix) {
        Stack<String> stack = new Stack<>(100);
        StringBuilder result = new StringBuilder();
        String[] tokens = infix.split(" ");

        for (String token : tokens) {
            if (isOperand(token)) {
                result.append(token).append(" ");
            } else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    result.append(stack.pop()).append(" ");
                }
                if (!stack.isEmpty()) stack.pop();
            } else if (isOperator(token)) {
                while (!stack.isEmpty() && !stack.peek().equals("(") &&
                        getPriority(stack.peek()) >= getPriority(token)) {
                    result.append(stack.pop()).append(" ");
                }
                stack.push(token);
            } else {
                // في الوضع العادي، نعتبر أي token ليس مشغلاً ولا قوساً هو معامل
                if (!isCustom) {
                    result.append(token).append(" ");
                } else {
                    throw new IllegalArgumentException("Invalid token in custom mode: " + token);
                }
            }
        }

        while (!stack.isEmpty()) {
            result.append(stack.pop()).append(" ");
        }

        return result.toString().trim();
    }

    public String infixToPrefix(String infix) {
        String reversed = reverseInfix(infix);
        String postfix = infixToPostfix(reversed);
        return reverseExpression(postfix);
    }

    public String postfixToInfix(String postfix) {
        Stack<String> stack = new Stack<>(100);
        String[] tokens = postfix.split(" ");

        for (String token : tokens) {
            if (isOperand(token) || (!isCustom && !isOperator(token))) {
                stack.push(token);
            } else if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Invalid postfix expression");
                }
                String op2 = stack.pop();
                String op1 = stack.pop();
                stack.push("( " + op1 + " " + token + " " + op2 + " )");
            } else {
                throw new IllegalArgumentException("Invalid token: " + token);
            }
        }
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid postfix expression");
        }
        return stack.pop();
    }

    public String prefixToInfix(String prefix) {
        Stack<String> stack = new Stack<>(100);
        String[] tokens = prefix.split(" ");

        for (int i = tokens.length - 1; i >= 0; i--) {
            String token = tokens[i];
            if (isOperand(token) || (!isCustom && !isOperator(token))) {
                stack.push(token);
            } else if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Invalid prefix expression");
                }
                String op1 = stack.pop();
                String op2 = stack.pop();
                stack.push("( " + op1 + " " + token + " " + op2 + " )");
            } else {
                throw new IllegalArgumentException("Invalid token: " + token);
            }
        }
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid prefix expression");
        }
        return stack.pop();
    }

    public String postfixToPrefix(String postfix) {
        String infix = postfixToInfix(postfix);
        return infixToPrefix(infix);
    }

    public String prefixToPostfix(String prefix) {
        String infix = prefixToInfix(prefix);
        return infixToPostfix(infix);
    }

    public double evaluatePostfix(String postfix) {
        // في الوضع المخصص، لا يمكن التقييم
        if (isCustom) {
            throw new UnsupportedOperationException("Evaluation not supported in custom mode");
        }

        Stack<Double> stack = new Stack<>(100);
        String[] tokens = postfix.split(" ");

        for (String token : tokens) {
            if (isNumeric(token)) {
                stack.push(Double.parseDouble(token));
            } else if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Invalid postfix expression");
                }
                double op2 = stack.pop();
                double op1 = stack.pop();
                stack.push(calculate(token, op1, op2));
            } else {
                throw new IllegalArgumentException("Invalid token: " + token);
            }
        }
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid postfix expression");
        }
        return stack.pop();
    }

    private boolean isOperand(String token) {
        if (isCustom) {
            // في الوضع المخصص، نتحقق من قائمة المعاملات
            for (String op : operands) {
                if (op.equals(token)) return true;
            }
            return false;
        }
        // في الوضع العادي، نعتبر أن أي token ليس مشغلاً هو معامل
        return !isOperator(token);
    }

    private boolean isOperator(String token) {
        for (String op : operators) {
            if (op.equals(token)) return true;
        }
        return false;
    }

    private int getPriority(String op) {
        for (int i = 0; i < operators.length; i++) {
            if (operators[i].equals(op)) return priorities[i];
        }
        return 0;
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double calculate(String op, double a, double b) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/":
                if (b == 0) throw new ArithmeticException("Division by zero");
                return a / b;
            case "%": return a % b;
            case "^": return Math.pow(a, b);
            case "&&": return (a != 0 && b != 0) ? 1.0 : 0.0;
            case "||": return (a != 0 || b != 0) ? 1.0 : 0.0;
            case "$":
            case "@":
            default:
                throw new UnsupportedOperationException("Operator '" + op + "' not supported for evaluation");
        }
    }

    private String reverseInfix(String infix) {
        StringBuilder reversed = new StringBuilder();
        String[] tokens = infix.split(" ");
        for (int i = tokens.length - 1; i >= 0; i--) {
            String token = tokens[i];
            if (token.equals("(")) reversed.append(") ");
            else if (token.equals(")")) reversed.append("( ");
            else reversed.append(token).append(" ");
        }
        return reversed.toString().trim();
    }

    private String reverseExpression(String expr) {
        StringBuilder reversed = new StringBuilder();
        String[] tokens = expr.split(" ");
        for (int i = tokens.length - 1; i >= 0; i--) {
            reversed.append(tokens[i]).append(" ");
        }
        return reversed.toString().trim();
    }
}