public class CommandFileInfo extends Command {
    //используется при передачи файла с клиента на сервер и обратно
    String name;
    long len;

    public CommandFileInfo(String name, long len) {
        this.name = name;
        this.len = len;
        this.typeCommand = "CommandFileInfo";
    }
}
