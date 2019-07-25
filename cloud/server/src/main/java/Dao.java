import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class Dao {

    static EntityManager entityManager; //один на все подключения

    public Dao() {
        this.entityManager = Persistence.createEntityManagerFactory("DatabasePgSql").createEntityManager();
    }

    public boolean findNameUsers(String name){
        Query q = entityManager.createNamedQuery("Users.findByName",Users.class);
        q.setParameter("name", name);
        return q.getResultList().size() > 0;
    }

    public boolean findNameByPassUsers(String name, String pass){
        Query q = entityManager.createNamedQuery("Users.findByNameByPass",Users.class);
        q.setParameter("name", name);
        q.setParameter("pass", pass);
        return q.getResultList().size() > 0;
    }

    public boolean createUser(String name, String pass){
        Users user = new Users();
        user.setName(name);
        user.setPass(pass);
        entityManager.getTransaction().begin();
        try{
            entityManager.persist(user);
            entityManager.getTransaction().commit();
        }
        catch (Exception ex) {
            entityManager.getTransaction().rollback();
            return false;
        }
        return true;
    }


}
