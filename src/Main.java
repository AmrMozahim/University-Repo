import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.geometry.Insets;
import java.io.*;

public class Main extends Application {
    private Converter converter = new Converter();
    private boolean isCustom = false;
    private String[] language = new String[0];
    private String[] customOps = new String[0];
    private int[] customPrio = new int[0];

    private TextArea input = new TextArea();
    private TextArea output = new TextArea();
    private ComboBox<String> type = new ComboBox<>();

    @Override
    public void start(Stage stage) {
        stage.setMaximized(true);
        stage.setTitle("Notation Converter");

        VBox main = new VBox(10);
        main.setPadding(new Insets(10));

        // Radio buttons
        HBox radioBox = new HBox(10);
        RadioButton normal = new RadioButton("Conventional");
        RadioButton custom = new RadioButton("Custom");
        ToggleGroup group = new ToggleGroup();
        normal.setToggleGroup(group);
        custom.setToggleGroup(group);
        normal.setSelected(true);
        radioBox.getChildren().addAll(normal, custom);

        // File buttons (for Custom only)
        HBox fileBox = new HBox(10);
        Button loadLang = new Button("Load Language");
        Button loadPrec = new Button("Load Precedence");
        fileBox.getChildren().addAll(loadLang, loadPrec);
        fileBox.setVisible(false);

        normal.setOnAction(e -> {
            isCustom = false;
            fileBox.setVisible(false);
            output.setText("Switched to Conventional mode (numbers only)");
        });

        custom.setOnAction(e -> {
            isCustom = true;
            fileBox.setVisible(true);

        });

        // Input
        input.setPromptText("Enter expression here");
        input.setPrefHeight(100);

        // Conversion type
        type.getItems().addAll(
                "Infix to Postfix",
                "Infix to Prefix",
                "Postfix to Infix",
                "Postfix to Prefix",
                "Prefix to Infix",
                "Prefix to Postfix"
        );
        type.setValue("Infix to Postfix");

        // Action buttons
        HBox buttonBox = new HBox(10);
        Button convert = new Button("Convert");
        Button evaluate = new Button("Evaluate");
        Button clear = new Button("Clear");
        Button saveReport = new Button("Save Report");
        buttonBox.getChildren().addAll(convert, evaluate, clear, saveReport);

        // Output
        output.setEditable(false);
        output.setPrefHeight(150);

        // Add everything
        main.getChildren().addAll(
                new Label("Select Mode:"),
                radioBox,
                fileBox,
                new Separator(),
                new Label("Input Expression:"),
                input,
                new Label("Conversion Type:"),
                type,
                buttonBox,
                new Separator(),
                new Label("Output:"),
                output
        );

        // Button actions
        convert.setOnAction(e -> convert());
        evaluate.setOnAction(e -> evaluate());
        clear.setOnAction(e -> {
            input.clear();
            output.clear();
        });
        saveReport.setOnAction(e -> saveReport());

        loadLang.setOnAction(e -> loadLanguage());
        loadPrec.setOnAction(e -> loadPrecedence());

        Scene scene = new Scene(main);
        stage.setScene(scene);
        stage.show();
    }

    private void convert() {
        String text = input.getText().trim();
        if (text.isEmpty()) {
            output.setText("Error: Empty input");
            return;
        }

        try {
            if (isCustom) {
                if (language == null || language.length == 0) {
                    output.setText("Error: Load language file first");
                    return;
                }
                if (customOps == null || customOps.length == 0) {
                    output.setText("Error: Load precedence file first");
                    return;
                }
                converter.setCustom(language, customOps, customPrio);
            } else {
                converter.resetToDefault();
            }

            String result = "";
            String convType = type.getValue();

            switch (convType) {
                case "Infix to Postfix":
                    result = converter.infixToPostfix(text);
                    break;
                case "Infix to Prefix":
                    result = converter.infixToPrefix(text);
                    break;
                case "Postfix to Infix":
                    result = converter.postfixToInfix(text);
                    break;
                case "Postfix to Prefix":
                    result = converter.postfixToPrefix(text);
                    break;
                case "Prefix to Infix":
                    result = converter.prefixToInfix(text);
                    break;
                case "Prefix to Postfix":
                    result = converter.prefixToPostfix(text);
                    break;
            }

            output.setText(result);

        } catch (Exception e) {
            output.setText("Error: " + e.getMessage());
        }
    }

    private void evaluate() {
        String text = input.getText().trim();
        if (text.isEmpty()) {
            output.setText("Error: Empty input");
            return;
        }

        try {
            // في الوضع المخصص، لا يمكن التقييم
            if (isCustom) {
                output.setText("Error: Evaluation is only available in Conventional mode (numbers only)");
                return;
            }

            // في الوضع العادي، يمكن التقييم
            converter.resetToDefault();

            // أولاً: تحويل إلى postfix
            String postfix = converter.infixToPostfix(text);

            // ثانياً: تقييم postfix
            double result = converter.evaluatePostfix(postfix);

            output.setText( "Postfix: " + postfix + "\n" + "Result: " + result);

        } catch (Exception e) {
            output.setText("Error: " + e.getMessage() +
                    "\n\nNote: Conventional mode only supports numbers (e.g., 2 + 3 * 4)");
        }
    }

    private void loadLanguage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load Language File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append(" ");
                }
                reader.close();

                String[] tokens = content.toString().trim().split("\\s+");
                language = tokens;

                output.setText("Language file loaded successfully!\n\n" );

            } catch (Exception e) {
                output.setText("Error loading language file: " + e.getMessage());
            }
        }
    }

    private void loadPrecedence() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load Precedence File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            try {
                // أولاً: حساب عدد المشغلات
                BufferedReader reader = new BufferedReader(new FileReader(file));
                int operatorCount = 0;
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split("\\s+");
                    // نتحقق أن السطر يحتوي على أولوية (رقم في النهاية)
                    if (parts.length >= 2) {
                        // نطرح 1 لأن آخر عنصر هو الأسبقية
                        operatorCount += (parts.length - 1);
                    }
                }
                reader.close();

                if (operatorCount == 0) {
                    output.setText("Error: No valid operators found in the file");
                    return;
                }

                // ثانياً: قراءة المشغلات وأولوياتها
                reader = new BufferedReader(new FileReader(file));
                customOps = new String[operatorCount];
                customPrio = new int[operatorCount];
                int index = 0;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split("\\s+");
                    if (parts.length < 2) continue;

                    // آخر عنصر هو الأولوية
                    String lastPart = parts[parts.length - 1];
                    int priority;

                    try {
                        priority = Integer.parseInt(lastPart);
                    } catch (NumberFormatException e) {
                        output.setText("Error: Priority must be a number. Found: '" + lastPart + "'");
                        reader.close();
                        return;
                    }

                    // إضافة كل مشغل في السطر (ما عدا الأخير)
                    for (int i = 0; i < parts.length - 1; i++) {
                        customOps[index] = parts[i];
                        customPrio[index] = priority;
                        index++;
                    }
                }
                reader.close();

                // عرض النتيجة
                StringBuilder result = new StringBuilder();
                result.append("Precedence file loaded successfully!");






                // البحث عن الأولويات الفريدة
                String priorities = "";
                for (int i = 0; i < customPrio.length; i++) {
                    String prioStr = String.valueOf(customPrio[i]);
                    if (!priorities.contains("|" + prioStr + "|")) {
                        priorities += "|" + prioStr + "|";

                        // جمع كل العمليات لهذه الأولوية
                        result.append("  Priority ").append(prioStr).append(": ");
                        String operatorsForThisPrio = "";

                        for (int j = 0; j < customOps.length; j++) {
                            if (customPrio[j] == customPrio[i]) {
                                operatorsForThisPrio += customOps[j] + " ";
                            }
                        }


                    }
                }

                output.setText(result.toString());

            } catch (Exception e) {
                output.setText("Error loading precedence file: " + e.getMessage());
            }
        }
    }

    private void saveReport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Report");
        chooser.setInitialFileName("conversion_report.txt");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        File file = chooser.showSaveDialog(null);
        if (file != null) {
            try {
                PrintWriter writer = new PrintWriter(file);
                writer.println("=== Notation Conversion Report ===");
                writer.println();
                writer.println("Mode: " + (isCustom ? "Custom" : "Conventional"));
                writer.println();
                writer.println("Input Expression: " + input.getText());
                writer.println("Conversion Type: " + type.getValue());
                writer.println("Output: " + output.getText());
                writer.println();

                if (isCustom) {
                    writer.println("=== Custom Configuration ===");
                    writer.println("Language File:");
                    writer.print("  Operands: ");
                    for (String s : language) {
                        writer.print(s + " ");
                    }
                    writer.println();
                    writer.println();
                    writer.println("Precedence File:");
                    writer.println("  Operators and Priorities:");
                    for (int i = 0; i < customOps.length; i++) {
                        writer.println("    " + customOps[i] + " -> " + customPrio[i]);
                    }
                } else {
                    writer.println("=== Conventional Mode ===");
                    writer.println("Operators: +, -, *, /, ^");
                    writer.println("Priorities: +:1, -:1, *:2, /:2, ^:3");
                }

                writer.println();
                writer.println("=================================");
                writer.close();

                output.setText("Report saved successfully to:\n" + file.getAbsolutePath());

            } catch (Exception e) {
                output.setText("Error saving report: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}