import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.animation.PathTransition;


public class FXMLLoadOperationController implements Initializable {

    @FXML
    private Canvas canvasClient;
    @FXML
    private Canvas canvasServer;
    @FXML
    private Circle cicleAnim;
    @FXML
    public AnchorPane mainPane;



    public void initialize(URL url, ResourceBundle rb) {
        Font f = new Font("Quicksand", 8);
        GraphicsContext gcClient = canvasClient.getGraphicsContext2D();
        gcClient.setStroke(Color.BLUE);
        gcClient.setLineWidth(3);
        gcClient.strokeRect(55, 30, 50, 20);
        gcClient.strokeRect(30, 55, 100, 30);
        gcClient.setLineWidth(1);
        gcClient.strokeText("Клиент", 60, 75);
        GraphicsContext gcServer = canvasServer.getGraphicsContext2D();
        gcServer.setStroke(Color.GREEN);
        gcServer.setLineWidth(3);
        gcServer.strokeRect(25, 30, 50, 20);
        gcServer.strokeRect(0, 55, 100, 30);
        gcServer.setLineWidth(1);
        gcServer.strokeText("Сервер", 30, 75);
        Path path = new Path();
        path.getElements().add(new MoveTo(0,0));
        path.getElements().add(new HLineTo(83));
        PathTransition transition = new PathTransition();
        transition.setNode(cicleAnim);
        transition.setCycleCount(PathTransition.INDEFINITE);
        transition.setPath(path);
        transition.setDuration(Duration.seconds(2));
        transition.setAutoReverse(true);
        transition.play();
    }
}
