import java.util.List;

public class CommandLoginToServer extends Command {
    private String cause;
    private boolean error;
    private List<String> listFile;

    public CommandLoginToServer() {
        this.typeCommand = "LoginToServer";
    }

    public List<String> getListFile() {
        return listFile;
    }

    public void setListFile(List<String> listFile) {
        this.listFile = listFile;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
