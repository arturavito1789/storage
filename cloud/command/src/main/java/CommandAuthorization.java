public class CommandAuthorization extends Command{

    private String username;
    private String password;

    public CommandAuthorization(String username, String password) {
        this.username = username;
        this.password = password;
        this.typeCommand = "Authorization";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
