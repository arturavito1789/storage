public class CommandReceiveDeleteNameFile extends Command  {
    //используется когда необходимо с сервера удалить файл
    String name;

    public CommandReceiveDeleteNameFile (String name) {
        this.name = name;
        this.typeCommand = "CommandReceiveDeleteNameFile";
    }
}
