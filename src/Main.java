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
        });

        custom.setOnAction(e -> {
            isCustom = true;
            fileBox.setVisible(true);
        });

        // Input
        input.setPromptText("Enter expression here (use spaces between tokens)");
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

        // Action buttons (for both modes)
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
                default:
                    output.setText("Error: Invalid conversion type");
                    return;
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
                // محاولة التحويل فقط
                String postfix = converter.infixToPostfix(text);
                output.setText("Evaluation not available in custom mode.\nPostfix: " + postfix);
                return;
            }

            // في الوضع العادي، يمكن التقييم
            converter.resetToDefault();
            String postfix = converter.infixToPostfix(text);
            double result = converter.evaluatePostfix(postfix);
            output.setText("Result: " + result + "\nPostfix: " + postfix);
        } catch (Exception e) {
            output.setText("Error: " + e.getMessage());
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
                output.setText("Language loaded: " + language.length + " operands\n" +
                        "Operands: " + String.join(" ", language));

            } catch (Exception e) {
                output.setText("Error: " + e.getMessage());
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
                    // نطرح 1 لأن آخر عنصر هو الأسبقية
                    operatorCount += (parts.length - 1);
                }
                reader.close();

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

                    int priority = Integer.parseInt(parts[parts.length - 1]);

                    // إضافة كل مشغل في السطر (ما عدا الأخير)
                    for (int i = 0; i < parts.length - 1; i++) {
                        customOps[index] = parts[i];
                        customPrio[index] = priority;
                        index++;
                    }
                }
                reader.close();

                // عرض المشغلات المحملة
                StringBuilder opsList = new StringBuilder();
                for (int i = 0; i < customOps.length; i++) {
                    opsList.append(customOps[i]).append("(").append(customPrio[i]).append(") ");
                }

                output.setText("Precedence loaded: " + operatorCount + " operators\n" +
                        "Operators: " + opsList.toString().trim());

            } catch (Exception e) {
                output.setText("Error: " + e.getMessage());
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
                writer.println("Mode: " + (isCustom ? "Custom" : "Conventional"));
                writer.println("Input Expression: " + input.getText());
                writer.println("Conversion Type: " + type.getValue());
                writer.println("Output: " + output.getText());

                if (isCustom) {
                    writer.println("\nCustom Configuration:");
                    writer.print("Language Operands: ");
                    for (String s : language) {
                        writer.print(s + " ");
                    }
                    writer.println();
                    writer.println("Operators and Priorities:");
                    for (int i = 0; i < customOps.length; i++) {
                        writer.println("  " + customOps[i] + " -> " + customPrio[i]);
                    }
                } else {
                    // Conventional mode
                    writer.println("\nConventional Mode Configuration:");
                    writer.println("Default Operators: + - * / ^");
                    writer.println("Default Priorities: +:1 -:1 *:2 /:2 ^:3");
                }

                writer.println("\nDate: " + new java.util.Date());
                writer.println("=================================");
                writer.close();

                output.setText("Report saved successfully to: " + file.getName());

            } catch (Exception e) {
                output.setText("Error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}