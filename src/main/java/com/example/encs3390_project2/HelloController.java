package com.example.encs3390_project2;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.scene.layout.Pane;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;

import java.io.File;
import java.util.List;

public class HelloController {
    private File dataFile;
    @FXML private Label choosenFileLabel;
    @FXML private Button fileChooserButton;
    @FXML private Button runProgramButton;
    @FXML private Label waitingTimeLabel;
    @FXML private Label turnaroundTimeLabel;
    @FXML private Label deadlockLabel;
    @FXML private ScrollPane ganttScroll;
    @FXML private Pane ganttContent;
    @FXML private Slider zoomSlider;
    @FXML private Label zoomValueLabel;
    @FXML private TableView<DeadLockState> deadlockTable;
    @FXML private TableColumn<DeadLockState, Number> colDetectionTime;
    @FXML private TableColumn<DeadLockState, String> colDeadlocked;
    @FXML private TableColumn<DeadLockState, String> colVictims;
    private Canvas ganttCanvas;
    private List<GanttSegment> currentTimeline = List.of();
    private static final double ROW_H = 55;
    private static final double TICK_H = 25;
    private static final double LEFT_PAD = 8;
    private static final double RIGHT_PAD = 8;
    private static final double END_GUTTER = 50;
    private static final double BASE_PX_PER_TIME = 12;
    private static final double MIN_PX_PER_TIME  = 4;
    private static final double MAX_CANVAS_W     = 20000;
    @FXML
    public void initialize() {
        colDetectionTime.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getDetectionTime()));
        colDeadlocked.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDeadlockedProcessesText()));
        colVictims.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getVictimsSelectedText()));
        colDetectionTime.setStyle("-fx-alignment: CENTER;");
        colDeadlocked.setStyle("-fx-alignment: CENTER;");
        colVictims.setStyle("-fx-alignment: CENTER;");
        colDetectionTime.setCellFactory(c -> new TableCell<DeadLockState, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else { setText(String.valueOf(item.intValue())); setAlignment(javafx.geometry.Pos.CENTER); }
            }
        });
        colDeadlocked.setCellFactory(c -> new TableCell<DeadLockState, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else { setText(item); setAlignment(javafx.geometry.Pos.CENTER); }
            }
        });
        colVictims.setCellFactory(c -> new TableCell<DeadLockState, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setGraphic(null); }
                else { setText(item); setAlignment(javafx.geometry.Pos.CENTER); }
            }
        });
        ganttCanvas = new Canvas(800, ROW_H + TICK_H);
        ganttContent.getChildren().add(ganttCanvas);
        ganttScroll.setPrefViewportHeight(ROW_H + TICK_H + 10);
        ganttContent.setPrefHeight(ROW_H + TICK_H);
        zoomSlider.setMax(4.0);
        zoomSlider.setMin(0.25);
        zoomSlider.valueProperty().addListener((obs, o, n) -> {
            double zoomValue = Math.min(n.doubleValue(), 4.0);
            zoomSlider.setValue(zoomValue);
            zoomValueLabel.setText(String.format("%.0f%%", zoomValue * 100));
            renderGanttCanvas();
        });
        ganttScroll.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (!e.isControlDown()) return;
            double factor = (e.getDeltaY() > 0) ? 1.10 : 1.0 / 1.10;
            double newZoom = zoomSlider.getValue() * factor;
            newZoom = Math.max(zoomSlider.getMin(), Math.min(zoomSlider.getMax(), newZoom));
            newZoom = Math.min(newZoom, 4.0);
            zoomSlider.setValue(newZoom);
            e.consume();
        });
        ganttScroll.viewportBoundsProperty().addListener((obs, o, n) -> renderGanttCanvas());
        renderGanttCanvas();
    }
    @FXML
    private void fitGantt() {
        if (currentTimeline == null || currentTimeline.isEmpty()) return;
        int totalEnd = Math.max(1, currentTimeline.get(currentTimeline.size() - 1).end);
        double viewportW = ganttScroll.getViewportBounds().getWidth();
        if (viewportW <= 1) return;
        double usable = Math.max(1, viewportW - (LEFT_PAD + RIGHT_PAD + END_GUTTER));
        double fitPxPerTime = usable / totalEnd;
        double newZoom = fitPxPerTime / BASE_PX_PER_TIME;
        newZoom = Math.max(zoomSlider.getMin(), Math.min(zoomSlider.getMax(), newZoom));
        zoomSlider.setValue(newZoom);
    }
    private void renderGanttCanvas() {
        double viewportW = ganttScroll.getViewportBounds().getWidth();
        if (viewportW <= 1) viewportW = 800;
        if (currentTimeline == null || currentTimeline.isEmpty()) {
            setCanvasSize(Math.max(viewportW, 800), ROW_H + TICK_H);
            drawBackground();
            return;
        }
        int totalEnd = Math.max(1, currentTimeline.get(currentTimeline.size() - 1).end);
        double pxPerTime = BASE_PX_PER_TIME * zoomSlider.getValue();
        pxPerTime = Math.max(pxPerTime, MIN_PX_PER_TIME);
        double maxAllowedPx = MAX_CANVAS_W / totalEnd;
        pxPerTime = Math.min(pxPerTime, maxAllowedPx);
        double contentW = LEFT_PAD + (totalEnd * pxPerTime) + RIGHT_PAD + END_GUTTER;
        contentW = Math.max(contentW, viewportW);
        setCanvasSize(contentW, ROW_H + TICK_H);
        drawBackground();
        var gc = ganttCanvas.getGraphicsContext2D();
        double x = LEFT_PAD;
        double lastTickRight = -1;
        final double tickGap = 6;
        for (GanttSegment s : currentTimeline) {
            int dur = Math.max(0, s.end - s.start);
            if (dur == 0) continue;
            double w = Math.max(1, dur * pxPerTime);
            gc.setStroke(Color.web("#334155"));
            gc.strokeRect(x, 0, w, ROW_H);
            if (s.pid >= 0) {
                String label = "P" + s.pid;
                double fontSize = fitFontSize(label, w);
                if (fontSize >= 7) {
                    gc.setFill(Color.web("#F1F5FF"));
                    gc.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, fontSize));
                    double textW = approxTextWidth(label, fontSize);
                    double tx = x + (w - textW) / 2.0;
                    double ty = ROW_H / 2.0 + fontSize / 3.0;
                    gc.fillText(label, tx, ty);
                }
            }
            double tickX = x + w;
            gc.setStroke(Color.web("#334155"));
            gc.strokeLine(tickX, ROW_H, tickX, ROW_H + 10);
            String t = String.valueOf(s.end);
            double tickFont = 10;
            double tW = approxTextWidth(t, tickFont);
            double desiredX = tickX - tW / 2.0;
            if (desiredX > lastTickRight + tickGap) {
                gc.setFill(Color.web("#94A3B8"));
                gc.setFont(javafx.scene.text.Font.font("System", tickFont));
                gc.fillText(t, desiredX, ROW_H + 22);
                lastTickRight = desiredX + tW;
            }
            x += w;
        }
        ganttContent.setPrefWidth(ganttCanvas.getWidth());
        ganttContent.setPrefHeight(ganttCanvas.getHeight());
    }
    private void setCanvasSize(double w, double h) {
        ganttCanvas.setWidth(w);
        ganttCanvas.setHeight(h);
    }
    private void drawBackground() {
        var gc = ganttCanvas.getGraphicsContext2D();
        gc.setFill(Color.web("#0F172A"));
        gc.fillRect(0, 0, ganttCanvas.getWidth(), ganttCanvas.getHeight());
    }
    private double fitFontSize(String text, double boxW) {
        double max = 16, min = 6;
        double size = (boxW - 10) / (0.6 * Math.max(1, text.length()));
        return Math.min(max, Math.max(min, size));
    }
    private double approxTextWidth(String text, double fontSize) {
        return 0.6 * fontSize * text.length();
    }
    @FXML
    protected void showFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a File");
        this.dataFile = fileChooser.showOpenDialog(fileChooserButton.getScene().getWindow());
        if (this.dataFile != null && this.dataFile.isFile() && this.dataFile.canRead()) {
            choosenFileLabel.setText(this.dataFile.getAbsolutePath());
            choosenFileLabel.setStyle("-fx-text-fill: #22c55e;");
            runProgramButton.setDisable(false);
        }
    }
    @FXML
    protected void runProgram() {
        fileChooserButton.setDisable(true);
        runProgramButton.setDisable(true);
        Task<SimulationResult> task = new Task<>() {
            @Override
            protected SimulationResult call() {
                return HelloLogic.run(dataFile);
            }
        };
        task.setOnSucceeded(e -> {
            SimulationResult res = task.getValue();
            waitingTimeLabel.setText(String.format("%.2f", res.averageWaitingTime));
            turnaroundTimeLabel.setText(String.format("%.2f", res.averageTurnAroundTime));
            deadlockLabel.setText(res.deadLockStates.isEmpty()
                    ? "None"
                    : (res.deadLockStates.size() + " state(s)"));
            deadlockTable.setItems(FXCollections.observableArrayList(res.deadLockStates));
            currentTimeline = res.timeLine;
            Platform.runLater(this::renderGanttCanvas);
            fileChooserButton.setDisable(false);
            runProgramButton.setDisable(false);
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            deadlockLabel.setText("Error: " + (ex == null ? "Unknown error" : ex.getMessage()));
            fileChooserButton.setDisable(false);
            runProgramButton.setDisable(false);
        });
        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }
    @FXML
    private void HoverInto() {
        runProgramButton.setStyle(
                "-fx-background-color: #D97706;" +
                        "-fx-text-fill: #111827;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 11 14 11 14;" +
                        "-fx-cursor: hand;"
        );
        runProgramButton.setTranslateY(-2);
    }
    @FXML
    private void HoverOut() {
        runProgramButton.setStyle(
                "-fx-background-color: #F59E0B;" +
                        "-fx-text-fill: #1F2937;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 11 14 11 14;" +
                        "-fx-cursor: hand;"
        );
        runProgramButton.setTranslateY(0);
    }
}
