public class CommandResponseAllFileServer extends Command {
    //ответ об успешном удалении всех файлов
    public boolean error;
    public CommandResponseAllFileServer() {
        this.typeCommand = "CommandResponseAllFileServer";
    }
}
