import java.io.Serializable;

public abstract class Command implements Serializable {
    //нужен чтоб использовать полифоризм
   protected String typeCommand;

}
