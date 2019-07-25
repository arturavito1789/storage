import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author Артур
 */
@Entity
@Table(name = "users")
@NamedQueries({
        @NamedQuery(name = "Users.findByName", query = "SELECT u FROM Users u WHERE u.name = :name")
        , @NamedQuery(name = "Users.findByNameByPass", query = "SELECT u FROM Users u WHERE u.name = :name and u.pass = :pass")})

public class Users implements Serializable {

    private static final long serialVersionUID = 1L;
    @Column(name = "name")
    private String name;
    @Column(name = "pass")
    private String pass;

    @Id
    @SequenceGenerator(name="users_id_seq",
            sequenceName="users_id_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator="users_id_seq")
    @Column(name = "id", updatable=false)
    private Integer id;

    public Users() {
    }

    public Users(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Users)) {
            return false;
        }
        Users other = (Users) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
}
