import javax.management.Notification;

public class CommandResponseAddNameFile extends Command  {
    //используется когда сервер получил файл от клиента и после этого сервер отсылает эту команду клиенту чтобы тот
    // обновил список файлов на сервере в своем интерфейсе
    String name;

    public CommandResponseAddNameFile () {
        this.typeCommand = "CommandResponseAddNameFile";
    }

}
