import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class FXMLServerController implements Initializable {

    @FXML
    private TextField port;
    @FXML
    private Text captionServer;

    Server server = null;

    public void initialize(URL url, ResourceBundle rb) {

    }


    @FXML
    private void startServer(ActionEvent event) {
        if ("".equals(port.getText())){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Укажите порт");
            alert.setContentText("Необходимо указать порт пример 8189");
            alert.showAndWait();
            return;
        }
        if(server !=null && server.isRun()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Сервер уже стартанул");
            alert.setContentText("Сервер уже находится в режиме выполнения");
            alert.showAndWait();
            return;
        }
        int intPort =Integer.parseInt(port.getText());
        if(server ==null){
           server = new Server(intPort, captionServer);
        }
        server.run();
    }

    @FXML
    private void stopServerBtn(ActionEvent event) {
        stopServer();
    }

    public void stopServer(){
        if(server !=null && server.isRun()){
           server.stop();
        }
    }

    @FXML
    private void closeApp(MouseEvent event) {
        stopServer();
        System.exit(0);
    }
}
