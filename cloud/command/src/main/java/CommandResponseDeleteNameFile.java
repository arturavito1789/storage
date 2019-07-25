public class CommandResponseDeleteNameFile extends Command  {
    //используется когда сервер получил команду от клиента на удаление файла, сервер удаляет файл и после этого сервер отсылает эту команду
    // клиенту чтобы тот обновил список файлов на сервере в своем интерфейсе
    private String name;
    private boolean error;

    public CommandResponseDeleteNameFile() {
        this.typeCommand = "CommandResponseDeleteNameFile";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
