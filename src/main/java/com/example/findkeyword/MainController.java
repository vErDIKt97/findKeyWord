package com.example.findkeyword;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeMap;

public class MainController {

    public File curFile;
    public BorderPane mainPane;
    public ChoiceBox<Integer> sheetNumber;
    ObservableList<Integer> sheetNumberRow = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    ObservableList<Integer> listNumberRow = FXCollections.observableArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    public FileChooser fileChooser = new FileChooser();
    public Button start;
    public TableView<String> tableKeyWord;
    public TableColumn<Integer, Integer> count;
    public TableColumn<String, String> word;
    public ProgressBar progressBar;
    public ChoiceBox<Integer> lineNumber;
    private final int cellMessageNumber = 9;
    private final int cellTextNumber = 4;
    private final int cellTypeMessageNumber = 3;
    @FXML
    private TextField filePath;

    @FXML
    protected void browseFile() {
        curFile = fileChooser.showOpenDialog(new Stage());
        if (curFile != null) {
            filePath.setText(curFile.getPath());
        } else {
            filePath.setText("");
        }
    }

    @FXML
    private void initialize() {
        lineNumber.setItems(listNumberRow);
        lineNumber.setValue(1);
        sheetNumber.setItems(sheetNumberRow);
        sheetNumber.setValue(1);
    }

    public void startAnalytics() {
        if (curFile != null) {
            Platform.runLater(() -> {
                tableKeyWord.getItems().clear();
                int messageNumber = lineNumber.getValue();
                double progress = 0;
                updateProgressBar(progress);
                Workbook workbook;
                try {
                    //noinspection resource
                    workbook = new XSSFWorkbook(curFile);
                } catch (IOException | InvalidFormatException e) {
                    throw new RuntimeException(e);
                }
                progress = progress + 0.1;
                updateProgressBar(progress);
                TreeMap<String, Integer> data = new TreeMap<>();
                try {
                    Sheet sheet = workbook.getSheetAt(sheetNumber.getValue() - 1);

                    for (Row row : sheet) {
                        if (row.getCell(cellMessageNumber).getCellType() == CellType.NUMERIC) {
                            if (row.getCell(cellMessageNumber).getNumericCellValue() == messageNumber &&
                                    row.getCell(cellTextNumber) != null &&
                                    row.getCell(cellTypeMessageNumber).getRichStringCellValue().getString().equals("UserMessage")) {
                                String string = row.getCell(cellTextNumber).getRichStringCellValue().getString().replaceAll("[-+.^:,!?()>\n\"{}\t]", "");
                                string = string.toLowerCase();
                                String[] rowString = string.split(" ");
                                for (String s : rowString) {
                                    if (data.containsKey(s)) {
                                        data.replace(s, data.get(s) + 1);
                                    } else {
                                        data.put(s, 1);
                                    }
                                }
                            }
                        }
                        progress = progress + (0.8 / sheet.getPhysicalNumberOfRows());
                        updateProgressBar(progress);
                    }
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText(String.valueOf(e));
                    alert.setContentText(Arrays.toString(e.getStackTrace()));
                    alert.showAndWait();
                }

                tableKeyWord.getItems().addAll(data.keySet());
                word.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()));
                //noinspection SuspiciousMethodCalls
                count.setCellValueFactory(cd -> new SimpleObjectProperty<>(data.get(cd.getValue())));
                tableKeyWord.refresh();
                updateProgressBar(1);
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не выбран файл");
            alert.setContentText("Выбери файл по кнопке \"Выбрать файл\"");
            alert.showAndWait();
        }
    }

    private void updateProgressBar(double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    }

    public void debug() {
        tableKeyWord.refresh();
    }
}