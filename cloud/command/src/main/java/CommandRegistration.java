public class CommandRegistration extends Command{

    private String username;
    private String password;

    public CommandRegistration(String username, String password) {
        this.username = username;
        this.password = password;
        this.typeCommand = "Registration";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
