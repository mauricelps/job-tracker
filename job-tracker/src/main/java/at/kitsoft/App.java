package at.kitsoft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class App extends Application {

    private static final String ACCENT = "#FFEF00";      // Kontur / Füllfarbe
    private static final String ACCENT_DARK = "#ffffffff";
    private static final String BACKGROUND = "#414141FF"; // generelle Hintergrundfarbe
    private static final String BUTTON_TEXT = "#141414"; // Textfarbe auf gelbem Button

    private static final String baseUrl = "https://gtracker.kitsoft.at/data/";
    private static final String dllUrl = baseUrl + "jobtracker.dll";
    private static final String iniUrl = baseUrl + "scs_ws_plugin.ini";
    
    private TextField tokenField;
    private TextField ets2Field;
    private TextField atsField;

    private Button selectEts2;
    private Button selectAts;
    private Button installBtn;

    private Stage primaryStage;

    private interface ProgressReporter {
        void reportProgress(double progress);
        void reportMessage(String message);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        AnchorPane root = new AnchorPane();
        root.setPrefSize(600, 400);

        // Hintergrundfarbe setzen
        root.setBackground(new Background(new BackgroundFill(Color.web(BACKGROUND), CornerRadii.EMPTY, Insets.EMPTY)));

        // Controls gemäß FXML-Layout (nur als Information diente die FXML, UI wird hier programmgesteuert aufgebaut)
        tokenField = new TextField();
        tokenField.setPromptText("Paste Token from Website");
        tokenField.setLayoutX(136.0);
        tokenField.setLayoutY(64.0);
        tokenField.setPrefWidth(330.0);
        tokenField.setPrefHeight(40.0);
        tokenField.setAlignment(Pos.CENTER);
        tokenField.setStyle(String.join(";",
                "-fx-background-color: transparent",
                "-fx-text-fill: " + ACCENT,
                "-fx-prompt-text-fill: rgba(255,239,0,0.6)",
                "-fx-border-color: " + ACCENT,
                "-fx-border-width: 1.5",
                "-fx-background-radius: 5",
                "-fx-border-radius: 5"
        ));

        installBtn = new Button("Install");
        installBtn.setLayoutX(532.0);
        installBtn.setLayoutY(356.0);
        installBtn.setPrefWidth(56); // ungefähr der Textbreite
        installBtn.setPrefHeight(28);
        installBtn.setStyle(String.join(";",
                "-fx-background-color: " + ACCENT,
                "-fx-text-fill: " + BUTTON_TEXT,
                "-fx-border-color: " + ACCENT,
                "-fx-background-radius: 5",
                "-fx-border-radius: 5"
        ));

        Label tokenLabel = new Label("Authentication Token");
        tokenLabel.setLayoutX(225.0);
        tokenLabel.setLayoutY(46.0);
        tokenLabel.setPrefWidth(152.0);
        tokenLabel.setPrefHeight(18.0);
        tokenLabel.setAlignment(Pos.CENTER);
        tokenLabel.setStyle("-fx-text-fill: " + ACCENT_DARK + ";");

        Label titleLabel = new Label("Job Tracker Installation");
        titleLabel.setLayoutX(1.0);
        titleLabel.setPrefWidth(600.0);
        titleLabel.setPrefHeight(24.0);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setStyle("-fx-text-fill: " + ACCENT + "; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label ets2Label = new Label("Euro Truck Simulator 2 Install Directory");
        ets2Label.setLayoutY(147.0);
        ets2Label.setPrefWidth(600.0);
        ets2Label.setPrefHeight(18.0);
        ets2Label.setAlignment(Pos.CENTER);
        ets2Label.setStyle("-fx-text-fill: " + ACCENT_DARK + ";");

        ets2Field = new TextField();
        ets2Field.setEditable(false);
        ets2Field.setPromptText("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Euro Truck Simulator 2");
        ets2Field.setLayoutX(134.0);
        ets2Field.setLayoutY(165.0);
        ets2Field.setPrefWidth(330.0);
        ets2Field.setPrefHeight(26.0);
        ets2Field.setStyle(String.join(";",
                "-fx-background-color: transparent",
                "-fx-text-fill: " + ACCENT,
                "-fx-prompt-text-fill: rgba(255,239,0,0.6)",
                "-fx-border-color: " + ACCENT,
                "-fx-border-width: 1.2",
                "-fx-background-radius: 5",
                "-fx-border-radius: 5"
        ));

        selectEts2 = new Button("Select");
        selectEts2.setLayoutX(466.0);
        selectEts2.setLayoutY(165.0);
        selectEts2.setPrefWidth(56);
        selectEts2.setPrefHeight(28);
        selectEts2.setStyle(String.join(";",
                "-fx-background-color: transparent",
                "-fx-text-fill: " + ACCENT,
                "-fx-border-color: " + ACCENT,
                "-fx-background-radius: 5",
                "-fx-border-radius: 5"
        ));

        Label atsLabel = new Label("American Truck Simulator Install Directory");
        atsLabel.setLayoutY(220.0);
        atsLabel.setPrefWidth(600.0);
        atsLabel.setPrefHeight(18.0);
        atsLabel.setAlignment(Pos.CENTER);
        atsLabel.setStyle("-fx-text-fill: " + ACCENT_DARK + ";");

        atsField = new TextField();
        atsField.setEditable(false);
        atsField.setPromptText("C:\\Program Files (x86)\\Steam\\steamapps\\common\\American Truck Simulator");
        atsField.setLayoutX(134.0);
        atsField.setLayoutY(238.0);
        atsField.setPrefWidth(330.0);
        atsField.setPrefHeight(26.0);
        atsField.setStyle(String.join(";",
                "-fx-background-color: transparent",
                "-fx-text-fill: " + ACCENT,
                "-fx-prompt-text-fill: rgba(255,239,0,0.6)",
                "-fx-border-color: " + ACCENT,
                "-fx-border-width: 1.2",
                "-fx-background-radius: 5",
                "-fx-border-radius: 5"
        ));

        selectAts = new Button("Select");
        selectAts.setLayoutX(466.0);
        selectAts.setLayoutY(238.0);
        selectAts.setPrefWidth(56);
        selectAts.setPrefHeight(28);
        selectAts.setStyle(String.join(";",
                "-fx-background-color: transparent",
                "-fx-text-fill: " + ACCENT,
                "-fx-border-color: " + ACCENT,
                "-fx-background-radius: 5",
                "-fx-border-radius: 5"
        ));

        selectEts2.setOnAction(e -> chooseDirectoryAndSet(ets2Field));
        selectAts.setOnAction(e -> chooseDirectoryAndSet(atsField));
        installBtn.setOnAction(e -> onInstall());

        // Alle Controls zum Root hinzufügen
        root.getChildren().addAll(asNodes(
                tokenField, installBtn, tokenLabel, titleLabel,
                ets2Label, ets2Field, selectEts2,
                atsLabel, atsField, selectAts
        ));



        // Optional: kleine globale Anpassung der Fokus- und Hover-Effekte (gelber Accent bleibt sichtbar)
        // Hier nicht übertrieben, damit das Styling simpel und konsistent bleibt.

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Job Tracker - Installation");
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.show();
    }

    private Node[] asNodes(Node... nodes) {
        return nodes;
    }

    private void chooseDirectoryAndSet(TextField targetField) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select Install Directory");

        if(!targetField.getText().isBlank()){
                Path p = Paths.get(targetField.getText());
                if(Files.isDirectory(p)){
                        dc.setInitialDirectory(p.toFile());
                }
        }
        File selectedDirectory = dc.showDialog(primaryStage);
        if (selectedDirectory != null) {
            targetField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    private void onInstall() {
        String ets2Path = ets2Field.getText().trim();
        String atsPath = atsField.getText().trim();
        String token = tokenField.getText().trim();

        boolean hasEts2 = !ets2Path.isEmpty();
        boolean hasAts = !atsPath.isEmpty();
        boolean hasToken = !token.isEmpty();

        if (!hasToken) {
            showAlert(Alert.AlertType.WARNING, "No Token", "Please paste your authentication token from the website.");
            return;
        }

        //TODO check token with webserver and save the corresponding user id locally so no re-auth is needed.

        if (!hasEts2 && !hasAts) {
            showAlert(Alert.AlertType.WARNING, "No Target Directories", "Please select at least one installation directory (ETS2 or ATS and include the bin/win_x64/plugins Folder!).");
            return;
        }

        // Deaktiviere UI-Elemente während Installation
        setControlsDisabled(true);

        // Liste der URLs, die heruntergeladen werden sollen
        List<String> urls = new ArrayList<>();
        urls.add(dllUrl);
        urls.add(iniUrl);

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Starte Download...");
                // Reporter, der updateProgress/updateMessage der Task aufruft
                ProgressReporter reporter = new ProgressReporter() {
                    @Override
                    public void reportProgress(double progress) {
                        updateProgress(progress, 1.0);
                    }

                    @Override
                    public void reportMessage(String message) {
                        updateMessage(message);
                    }
                };
                BooleanSupplier cancelled = this::isCancelled;

                // Erzeuge temporäres Verzeichnis für die Downloads
                Path tempDir = Files.createTempDirectory("job-tracker-downloads-");
                try {
                    // Bestimme Gesamtgröße (falls möglich) für kombinierte Progressanzeige
                    java.util.concurrent.atomic.AtomicLong totalKnownBytes = new java.util.concurrent.atomic.AtomicLong(0);
                    boolean anyUnknown = false;
                    List<Long> lengths = new ArrayList<>();
                    for (String url : urls) {
                        long len = queryContentLength(url);
                        lengths.add(len);
                        if (len < 0) anyUnknown = true;
                        else totalKnownBytes.addAndGet(Math.max(0, len));
                    }

                    java.util.concurrent.atomic.AtomicLong downloadedSoFar = new java.util.concurrent.atomic.AtomicLong(0L);
                    for (int i = 0; i < urls.size(); i++) {
                        if (isCancelled()) throw new IOException("Abgebrochen.");
                        String url = urls.get(i);
                        reporter.reportMessage("Bestimme Dateiname für " + url + "...");
                        String fileName = getFileNameFromURLorConnection(url);
                        reporter.reportMessage("Lade herunter: " + fileName);
                        Path out = tempDir.resolve(fileName);

                        if (!anyUnknown && totalKnownBytes.get() > 0) {
                            // wir können einen gewichteten Fortschritt berechnen
                            long fileLength = lengths.get(i);
                            downloadWithProgressWeighted(url, out, (bytesReadThisFile) -> {
                                double progress = (double) (downloadedSoFar.get() + bytesReadThisFile) / (double) totalKnownBytes.get();
                                reporter.reportProgress(progress);
                                reporter.reportMessage(String.format("Download %s: %.0f%%", fileName, progress * 100.0));
                            }, cancelled);
                            downloadedSoFar.addAndGet(Math.max(0, fileLength));
                            reporter.reportProgress((double) downloadedSoFar.get() / (double) totalKnownBytes.get());
                        } else {
                            // Größe unbekannt -> mache indeterminaten Fortschritt pro Datei
                            reporter.reportProgress(-1.0);
                            downloadWithProgress(url, out, reporter, cancelled);
                            reporter.reportProgress(-1.0); // bleibt indeterminate until copy
                        }

                        reporter.reportMessage("Download abgeschlossen: " + fileName);

                        // Kopiere die gerade heruntergeladene Datei in die ausgewählten Zielverzeichnisse
                        if (hasEts2) {
                            reporter.reportMessage("Kopiere " + fileName + " nach ETS2...");
                            copyToTarget(out, Paths.get(ets2Path), fileName);
                            reporter.reportMessage("Kopiert nach ETS2: " + ets2Path + "/" + fileName);
                        }
                        if (hasAts) {
                            reporter.reportMessage("Kopiere " + fileName + " nach ATS...");
                            copyToTarget(out, Paths.get(atsPath), fileName);
                            reporter.reportMessage("Kopiert nach ATS: " + atsPath + "/" + fileName);
                        }
                    }

                    reporter.reportMessage("Installation abgeschlossen.");
                    reporter.reportProgress(1.0);
                } finally {
                    // Lösche temporäres Verzeichnis und Inhalte
                    try {
                        deleteRecursively(tempDir);
                    } catch (IOException ignored) {
                    }
                }
                return null;
            }
        };

        // Progress-Dialog
        Stage dialog = createProgressDialog(task);
        task.setOnSucceeded(ev -> {
            dialog.close();
            setControlsDisabled(false);
            StringBuilder sb = new StringBuilder("Installation erfolgreich in folgende Verzeichnisse kopiert:\n");
            if (hasEts2) sb.append("- ").append(ets2Path).append("\n");
            if (hasAts) sb.append("- ").append(atsPath).append("\n");
            showAlert(Alert.AlertType.INFORMATION, "Erfolg", sb.toString());
        });
        task.setOnFailed(ev -> {
            dialog.close();
            setControlsDisabled(false);
            Throwable ex = task.getException();
            String msg = ex != null ? ex.getMessage() : "Unbekannter Fehler";
            showAlert(Alert.AlertType.ERROR, "Fehler bei Installation", "Es ist ein Fehler aufgetreten: " + msg);
        });

        Thread t = new Thread(task, "install-task");
        t.setDaemon(true);
        t.start();
        dialog.show();
    }

    private void setControlsDisabled(boolean disabled) {
        Platform.runLater(() -> {
            selectAts.setDisable(disabled);
            selectEts2.setDisable(disabled);
            installBtn.setDisable(disabled);
            tokenField.setDisable(disabled);
            ets2Field.setDisable(disabled);
            atsField.setDisable(disabled);
        });
    }

    private Stage createProgressDialog(Task<?> task) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Installation running...");

        ProgressBar bar = new ProgressBar();
        bar.setPrefWidth(380);
        bar.progressProperty().bind(task.progressProperty());

        Label message = new Label();
        message.setPrefWidth(380);
        message.textProperty().bind(task.messageProperty());

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> {
            task.cancel(true);
            dialog.close();
            setControlsDisabled(false);
        });

        AnchorPane pane = new AnchorPane();
        pane.setPadding(new Insets(10));
        pane.setPrefSize(400, 120);
        pane.getChildren().addAll(bar, message, cancel);

        AnchorPane.setTopAnchor(message, 10.0);
        AnchorPane.setLeftAnchor(message, 10.0);

        AnchorPane.setTopAnchor(bar, 40.0);
        AnchorPane.setLeftAnchor(bar, 10.0);

        AnchorPane.setTopAnchor(cancel, 80.0);
        AnchorPane.setRightAnchor(cancel, 10.0);

        Scene scene = new Scene(pane);
        dialog.setScene(scene);

        // Wenn Task fertig ist automatisch schließen (erledigt wird in handlers)
        return dialog;
    }

    /**
     * Download mit einfachem Fortschritt, verwendet Content-Length wenn vorhanden.
     */
    private void downloadWithProgress(String urlStr, Path dest, ProgressReporter reporter, BooleanSupplier cancelled) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "JobTrackerInstaller/1.0");
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(30_000);
        conn.connect();

        int response = conn.getResponseCode();
        if (response >= 400) {
            throw new IOException("Error whilst downloading: HTTP " + response);
        }

        long contentLength = conn.getContentLengthLong(); // -1 wenn unbekannt

        try (InputStream in = conn.getInputStream()) {
            // Wenn Content-Length bekannt -> Fortschritt berechnen
            if (contentLength > 0) {
                try (var out = Files.newOutputStream(dest, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    byte[] buffer = new byte[16 * 1024];
                    long totalRead = 0;
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        if (cancelled.getAsBoolean()) throw new IOException("Download cancelled.");
                        out.write(buffer, 0, read);
                        totalRead += read;
                        double progress = (double) totalRead / (double) contentLength;
                        reporter.reportProgress(progress);
                        reporter.reportMessage(String.format("Download: %.0f%%", progress * 100.0));
                    }
                    reporter.reportProgress(1.0);
                }
            } else {
                // Content length unknown -> no reliable progress indication
                reporter.reportMessage("Download (size unknown)...");
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                reporter.reportProgress(1.0);
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Kopiert die temporäre Datei in das Zielverzeichnis. Der Zielname entspricht dem Dateinamen.
     */
    private void copyToTarget(Path sourceFile, Path targetDirectory) throws IOException {
        if (!Files.exists(targetDirectory)) {
            throw new IOException("Target directory does not exist: " + targetDirectory);
        }
        if (!Files.isDirectory(targetDirectory)) {
            throw new IOException("Target is not a directory: " + targetDirectory);
        }
        Path targetFile = targetDirectory.resolve(sourceFile.getFileName());
        Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
    }

    private String getFileNameFromURL(String urlStr) {
        try {
            URL url = new URL(urlStr);
            String path = url.getPath();
            int idx = path.lastIndexOf('/');
            if (idx >= 0 && idx + 1 < path.length()) {
                return path.substring(idx + 1);
            }
        } catch (Exception ignored) {
        }
        // Fallback
        return "downloaded-file.bin";
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.initOwner(primaryStage);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    private void deleteRecursively(Path dir) throws IOException {
        if (dir == null || !Files.exists(dir)) return;
        Files.walk(dir)
                .sorted((a, b) -> b.compareTo(a)) // deepest first
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
    }

    private void copyToTarget(Path sourceFile, Path targetDirectory, String fileName) throws IOException {
        if (!Files.exists(targetDirectory)) {
            throw new IOException("Zielverzeichnis existiert nicht: " + targetDirectory);
        }
        if (!Files.isDirectory(targetDirectory)) {
            throw new IOException("Ziel ist kein Verzeichnis: " + targetDirectory);
        }
        Path targetFile = targetDirectory.resolve(fileName);
        Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
    }

    private String getFileNameFromURLorConnection(String urlStr) {
        // Versuche Content-Disposition zuerst
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);
            conn.connect();
            String cd = conn.getHeaderField("Content-Disposition");
            conn.disconnect();
            if (cd != null) {
                Matcher m = Pattern.compile("filename\\*?=(?:UTF-8'')?\"?([^\";\\n]+)\"?").matcher(cd);
                if (m.find()) {
                    return Paths.get(m.group(1)).getFileName().toString();
                }
            }
        } catch (Exception ignored) {
        }
        // Fallback: Namen aus URL-Pfad
        return getFileNameFromURL(urlStr);
    }

    private long queryContentLength(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(10_000);
            conn.connect();
            long len = conn.getContentLengthLong();
            conn.disconnect();
            return len;
        } catch (Exception e) {
            return -1;
        }
    }

    private void downloadWithProgressWeighted(String urlStr, Path dest, FileBytesCallback bytesReadCallback, BooleanSupplier cancelled) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "JobTrackerInstaller/1.0");
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(30_000);
        conn.connect();

        int response = conn.getResponseCode();
        if (response >= 400) {
            conn.disconnect();
            throw new IOException("Fehler beim Herunterladen: HTTP " + response);
        }

        long contentLength = conn.getContentLengthLong(); // sollte >0 hier sein
        try (InputStream in = conn.getInputStream();
             var out = Files.newOutputStream(dest, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            byte[] buffer = new byte[16 * 1024];
            long totalRead = 0;
            int read;
            while ((read = in.read(buffer)) != -1) {
                if (cancelled.getAsBoolean()) throw new IOException("Download abgebrochen.");
                out.write(buffer, 0, read);
                totalRead += read;
                bytesReadCallback.onBytesRead(totalRead);
            }
            // final callback to signal completion of this file
            bytesReadCallback.onBytesRead(totalRead);
        } finally {
            conn.disconnect();
        }
    }

    @FunctionalInterface
    private interface FileBytesCallback {
        void onBytesRead(long bytesReadForThisFile);
    }

    public static void main(String[] args) {
        launch();
    }
}