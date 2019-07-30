package simple;

/**
 * @Author hbs
 * @Date 2019/7/30
 */
public class RejectExecutionHandler {
    public void rejectedExecution(){
        throw new RuntimeException();
    }
}
