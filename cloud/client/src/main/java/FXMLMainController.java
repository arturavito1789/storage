import com.jfoenix.controls.JFXListView;
import io.netty.buffer.Unpooled;
import io.netty.channel.EventLoopGroup;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tray.animations.AnimationType;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;
import javafx.util.Duration;
import javafx.stage.StageStyle;




public class FXMLMainController implements Initializable {


    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private CheckBox isRegister;


    @FXML
    private StackPane stagePannel_1;

    @FXML
    private StackPane stagePannel_2;

    @FXML
    private Text captionConnect;

    @FXML
    private JFXListView listClient;

    @FXML
    private JFXListView listServer;

    private Stage dialogStageLoadOperation;

    public String mainPath;

    Client client = null;

    public void initialize(URL url, ResourceBundle rb) {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        this.mainPath = s.substring(0, s.lastIndexOf("cloud")+5) + "\\" + "klient file" + "\\";
    }

    public void setTextCaptionConnect(String str){
        captionConnect.setText("Параметры подключения " + str);
    }


    @FXML
    private void connectServer(ActionEvent event) {

        String strUsername = username.getText();
        if ("".equals(strUsername)){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Укажите имя пользователя");
            alert.setContentText("Необходимо указать имя пользователя");
            alert.showAndWait();
            return;
        }
        String strPassword = password.getText();
        if ("".equals(strPassword)){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("strPassword");
            alert.setHeaderText("Укажите свой пароль");
            alert.setContentText("Необходимо указать свой пароль");
            alert.showAndWait();
            return;
        }

        //подключаемся
        client = new Client(this);
        client.run();

        //ждем чтоб подключение установилось тогда client.isRun() вернет true
        try {
            client.operationServer.await();
        } catch (InterruptedException e) {
            setTextCaptionConnect("(не удалось подключиться)");
            e.printStackTrace();
            return;
        }

        //подключение либо успешно либо нет
        if (client.isRun() == false){
            setTextCaptionConnect("(не удалось подключиться)");
            return;
        }

        //проходим авторизацию

        Command command = null;

        if(isRegister.isSelected()== true){
            command = new CommandRegistration(strUsername, strPassword);
        }
        else{
            command = new CommandAuthorization(strUsername, strPassword);
        }

        sendCommand(command);

    }

    @FXML
    private void closeApp(MouseEvent event) {
        System.exit(0);
    }

    public void showStageWork(List<String> listFile){
        stagePannel_1.setVisible(false);
        stagePannel_2.setVisible(true);
        Path path = Paths.get(mainPath);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    long fileSize = file.toFile().length();
                    String absolutePath = file.toAbsolutePath().toString();
                    if (!file.toFile().isDirectory()){
                        listClient.getItems().add(fileName);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (listFile != null) {
            for (String str : listFile) {
                listServer.getItems().add(str);
            }
        }
        Platform.runLater(new Runnable() {
            @Override public void run() {
                try {
                    dialogStageLoadOperation = new Stage();
                    dialogStageLoadOperation.initStyle(StageStyle.TRANSPARENT);
                    Parent root = FXMLLoader.load(getClass().getResource("LoadOperation.fxml"));
                    Scene scene = new Scene(root, Color.TRANSPARENT);
                    dialogStageLoadOperation.setScene(scene);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean startOperationServer(){
        if (client.getGroup().isShutdown() == true){
            visibilityNotification ("Нет подключения с сервером", "Нет подключения с сервером перезапустите программу");
            return false;
        }
        if (client.operationServer.getCount() > 0){
            visibilityNotification ("Идет операция с сервером", "Дождитесь окончания операции с сервером");
            return false;
        }
        return true;
    }

    public  void updateCustromlist(JFXListView list, String selItem, boolean add){
        //все что связано с интерфейсом нужно вызывать в спец потоке
        client.operationServer.countDown();
        Platform.runLater(new Runnable() {
            @Override public void run() {
                if(add == true) {
                    list.getItems().add(selItem);
                } else {
                    list.getItems().remove(list.getItems().indexOf(selItem));
                }
                dialogStageLoadOperation.close();
            }
        });
    }

    public  void addItemlistServer(String selItem){
        //все что связано с интерфейсом нужно вызывать в спец потоке
        updateCustromlist(listServer, selItem, true);
    }

    public  void deleteItemlistServer(String selItem){
        //все что связано с интерфейсом нужно вызывать в спец потоке
        updateCustromlist(listServer, selItem, false);
    }

    public  void addItemlistClient(String selItem){
        //все что связано с интерфейсом нужно вызывать в спец потоке
        updateCustromlist(listClient, selItem, true);
    }

    public  void clearlistClient(){
        //очишаем список клиента и заполняем его списком из сервера
        client.operationServer.countDown();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                dialogStageLoadOperation.close();
                listClient.getItems().clear();
                listClient.getItems().addAll(listServer.getItems());
            }
        });
    }

    public  void clearlistServer(boolean error){
        //очишаем список из сервера и заполняем его списком из клиента
        client.operationServer.countDown();
        if (error==true){
            visibilityNotification ("Не удалось удалить файлы", "На сервере не удалось удалить файлы");
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                dialogStageLoadOperation.close();
                if (error== false){
                    listServer.getItems().clear();
                    listServer.getItems().addAll(listClient.getItems());
                }
            }
        });
    }


    @FXML
    private void addFile(MouseEvent event) {
        //добавить файл на клиенте в папку для обмена с сервером
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile.exists()){
            InputStream is = null;
            OutputStream os = null;
            try {
                String fileName = selectedFile.getName();
                is = new FileInputStream(selectedFile);
                os = new FileOutputStream(mainPath + fileName);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
                listClient.getItems().add(fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void deleteFileClient(MouseEvent event) {
       //удалить файл из клиентской папки для обмена с сервером
        Object selItem = listClient.getSelectionModel().getSelectedItem();
        if (selItem != null){
            File file = new File(mainPath + selItem.toString());
            file.delete();
            listClient.getItems().remove(selItem);
        }else{
            visibilityNotification ("Выделите файл", "Перед удалением необходимо выделить файл в локальном хранилище");
        }
    }

    @FXML
    private void deleteFileServer(MouseEvent event) {
        //удалить файл из с сервера
        boolean res = startOperationServer();
        if (res == false){
            return;
        }
        //отсылаем на сервер команду, сервер удаляет и отправляет обратно результат на его основе мы удаляем имя файла из списка
        Object selItem = listServer.getSelectionModel().getSelectedItem();
        if (selItem != null){
            CommandReceiveDeleteNameFile commandReceiveDeleteNameFile = new CommandReceiveDeleteNameFile(selItem.toString());
            sendCommand(commandReceiveDeleteNameFile);
        }else{
            visibilityNotification ("Выделите файл", "Перед удалением необходимо выделить файл в серверном хранилище");
        }
    }

    @FXML
    private void downloadToClient(MouseEvent event) {
        //передать файл с сервера на клиент
        boolean res = startOperationServer();
        if (res == false){
            return;
        }
        Object selItem = listServer.getSelectionModel().getSelectedItem();
        if (selItem != null){
            CommandReceiveNameFile сommandReceiveNameFile = new CommandReceiveNameFile(selItem.toString());
            sendCommand(сommandReceiveNameFile);
        }else{
            visibilityNotification ("Выделите файл", "Необходимо выделить файл в серверном хранилище");
        }
    }

    @FXML
    private void downloadToServer(MouseEvent event) {
        //передать файл с клиента на сервер
        boolean res = startOperationServer();
        if (res == false){
            return;
        }
        Object selItem = listClient.getSelectionModel().getSelectedItem();
        if (selItem != null){
            try {
                String nameF = selItem.toString();
                sendFile(nameF);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            visibilityNotification ("Выделите файл", "Необходимо выделить файл в локальном хранилище");
        }
    }

    @FXML
    private void sinxronToClient(MouseEvent event) {
        //передать все файлы с клиента на сервер
        boolean res = startOperationServer();
        if (res == false){
            return;
        }
        if (listClient.getItems().size() == 0){
            visibilityNotification ("На клиенте нет файлов", "Список файлов клиента пуст");
            return;
        }
        CommandReceiveAllFileServer commandReceiveAllFileServer = new CommandReceiveAllFileServer();
        sendCommand(commandReceiveAllFileServer);
    }

    @FXML
    private void sinxronToServer(MouseEvent event) {
        //передать все файлы с сервера на клиент
        boolean res = startOperationServer();
        if (res == false){
            return;
        }
        if (listServer.getItems().size() == 0){
            visibilityNotification ("На сервере нет файлов", "Список файлов сервера пуст");
            return;
        }
        Path path = Paths.get(mainPath);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (!file.toFile().isDirectory()){
                        File fileD = file.toFile();
                        fileD.delete();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.handler.goTransferListFile = true;
        CommandReceiveAllClient сommandReceiveAllClient = new CommandReceiveAllClient();
        sendCommand(сommandReceiveAllClient);
    }

    public void sendFile(String nameF){
        client.operationServer = new CountDownLatch(1);
        dialogStageLoadOperation.show();
        client.handler.sendFile(nameF);
    }

    public void sendCommand(Command command){
        if (dialogStageLoadOperation !=null) {
            client.operationServer = new CountDownLatch(1);
            dialogStageLoadOperation.show();
        }
        client.handler.sendCommand(command);
        //потчти на каждую команду сервер должен ответить. Ответ обрабатывается в Handler > channelRead
    }

    public void visibilityNotification (String title, String message){
        TrayNotification tray = new TrayNotification();
        tray.setTitle(title);
        tray.setMessage(message);
        tray.setNotificationType(NotificationType.INFORMATION);
        tray.setAnimationType(AnimationType.POPUP);
        tray.showAndDismiss(Duration.seconds(5));
    }

}
