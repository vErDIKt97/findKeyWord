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
    public ChoiceBox<String> sheetNumber;
    public BorderPane mainPaneSolution;
    public ProgressBar progressBarSolution;
    public TextField filePathSolution;
    public Button browseFileSolution;
    public ChoiceBox<String> sheetNumberSolution;
    public ChoiceBox<String> countWord;
    public Button startSolution;
    public TableView<String> tableKeyWordSolution;
    public TableColumn<Integer, Integer> countSolution;
    public TableColumn<String, String> wordSolution;
    public ChoiceBox<String> countWordSequence;
    public ChoiceBox<String> fromUser;
    ObservableList<String> sheetNumberRow = FXCollections.observableArrayList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10");
    ObservableList<String> listNumberRow = FXCollections.observableArrayList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Любой");
    ObservableList<String> senderMessage = FXCollections.observableArrayList("Клиент", "Техподдержка");
    public FileChooser fileChooser = new FileChooser();
    public Button start;
    public TableView<String> tableKeyWord;
    public TableColumn<Integer, Integer> count;
    public TableColumn<String, String> word;
    public ProgressBar progressBar;
    public ChoiceBox<String> lineNumber;
    private final int cellMessageNumber = 10;
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
        lineNumber.setValue(listNumberRow.get(0));
        sheetNumber.setItems(sheetNumberRow);
        sheetNumber.setValue(sheetNumberRow.get(0));
        countWord.setItems(sheetNumberRow);
        countWord.setValue(sheetNumberRow.get(0));
        countWordSequence.setItems(sheetNumberRow);
        countWordSequence.setValue(sheetNumberRow.get(0));
        sheetNumberSolution.setItems(sheetNumberRow);
        sheetNumberSolution.setValue(sheetNumberRow.get(0));
        fromUser.setItems(senderMessage);
        fromUser.setValue(senderMessage.get(0));
    }

    public void startAnalytics() {
        if (curFile != null) {
            Platform.runLater(() -> {
                tableKeyWord.getItems().clear();
                int messageNumber = parseIntChoiceBoxSt(lineNumber);
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
                    Sheet sheet = workbook.getSheetAt(parseIntChoiceBoxSt(sheetNumber) - 1);

                    for (Row row : sheet) {
                        if (row.getCell(cellMessageNumber).getCellType() == CellType.NUMERIC) {
                            if (messageNumber != 0) {
                                if (row.getCell(cellMessageNumber).getNumericCellValue() == messageNumber &&
                                        row.getCell(cellTextNumber) != null &&
                                        row.getCell(cellTypeMessageNumber).getRichStringCellValue().getString().equals(getSender(fromUser.getValue()))) {
                                    fillData(data, row);
                                }
                            } else {
                                if (row.getCell(cellTextNumber) != null &&
                                        row.getCell(cellTypeMessageNumber).getRichStringCellValue().getString().equals(getSender(fromUser.getValue()))) {
                                    fillData(data, row);
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

    private void fillData(TreeMap<String, Integer> data, Row row) {
        String string = clearRow(row, cellTextNumber, false);
        string = string.toLowerCase();
        String[] rowString = string.split(" ");
        if (rowString.length > parseIntChoiceBoxSt(countWordSequence)) {
            for (int i = 0; i < rowString.length - 1; i++) {
                String stringSequence = getStringSequence(rowString, parseIntChoiceBoxSt(countWordSequence), i);
                if (data.containsKey(stringSequence)) {
                    data.replace(stringSequence, data.get(stringSequence) + 1);
                } else {
                    data.put(stringSequence, 1);
                }
            }
        }
    }

    private int parseIntChoiceBoxSt(ChoiceBox<String> choiceBox) {
        if (choiceBox.getValue().equals("Любой"))
            return 0;
        else
            return Integer.parseInt(choiceBox.getValue());
    }

    private void updateProgressBar(double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    }

    public void debug() {
        tableKeyWord.refresh();
    }

    public String getSender(String user) throws Exception {
        if (user.equals("Клиент")) {
            return "UserMessage";
        } else if (user.equals("Техподдержка"))
            return "TechnicianMessage";
        else throw new Exception("Не корректно заполнено поле \"Сообщение от\"!");
    }

    // Incident solution


    @FXML
    protected void browseFileSolution() {
        curFile = fileChooser.showOpenDialog(new Stage());
        if (curFile != null) {
            filePathSolution.setText(curFile.getPath());
        } else {
            filePathSolution.setText("");
        }
    }

    @FXML
    public void startAnalyticsSolution() {
        if (curFile != null) {
            Platform.runLater(() -> {
                tableKeyWordSolution.getItems().clear();
                int messageNumber = parseIntChoiceBoxSt(countWord);
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
                    Sheet sheet = workbook.getSheetAt(parseIntChoiceBoxSt(sheetNumberSolution) - 1);
                    for (Row row : sheet) {
                        String string = clearRow(row, 0, true);
                        String[] rowString = string.split(" ");
                        if (rowString.length > parseIntChoiceBoxSt(countWord)) {
                            data.put(getStringSequence(rowString, parseIntChoiceBoxSt(countWord), 0), 1);
                            for (int i = 1; i < rowString.length - 1; i++) {
                                if (rowString.length >= i + 1) {
                                    String stringSequence = getStringSequence(rowString, parseIntChoiceBoxSt(countWord), i);
                                    if (data.containsKey(stringSequence)) {
                                        data.replace(stringSequence, data.get(stringSequence) + 1);
                                    } else {
                                        data.put(stringSequence, 1);
                                    }
                                }
                                progress = progress + (0.8 / sheet.getPhysicalNumberOfRows());
                                updateProgressBar(progress);
                            }
                        }
                    }
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText(String.valueOf(e));
                    alert.setContentText(Arrays.toString(e.getStackTrace()));
                    alert.showAndWait();
                }

                tableKeyWordSolution.getItems().addAll(data.keySet());
                wordSolution.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()));
                //noinspection SuspiciousMethodCalls
                countSolution.setCellValueFactory(cd -> new SimpleObjectProperty<>(data.get(cd.getValue())));
                tableKeyWordSolution.refresh();
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

    private String clearRow(Row row, int targetCell, boolean fromCell) {
        String string = row.getCell(targetCell).getRichStringCellValue().getString();

        if (fromCell) {
            for (int i = 1; i < 5; i++) {
                string = string.replace(row.getCell(i).getRichStringCellValue().getString(), "");
            }
        }
        string = string.replace("Создано СМС с просьбой перезвонить.  Проверьте статус сообщения в пункте «СМС». Если СМС доставлено, то ждите звонка абонента до", " ");
        string = string.replace("Если этого не произойдет, то перезвоните абоненту. Если СМС не доставлено, то перезвоните абоненту.", " ");
        string = string.replaceAll("[-+.^:,!?()>\n\"{}\t0123456789№%/*=#<;_]", "");
        string = string.replace("     ", " ");
        string = string.replace("    ", " ");
        string = string.replace("   ", " ");
        string = string.replace("  ", " ");
        string = string.replace("  ", " ");
        string = string.toLowerCase();
        return string;
    }

    public String getStringSequence(String[] rowString, Integer countWord, int i) {
        String string = "";
        if (rowString.length > i + countWord) {
            for (int j = 0; j < countWord; j++) {
                string = string + " " + rowString[i];
                i++;
            }
        }
        return string;
    }

}