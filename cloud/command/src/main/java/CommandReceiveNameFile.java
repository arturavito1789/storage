class CommandReceiveNameFile extends Command {
    //используется когда у сервера запрашиваем какой небудь файл
    String name;

    public CommandReceiveNameFile (String name) {
        this.name = name;
        this.typeCommand = "CommandReceiveNameFile";
    }
}
