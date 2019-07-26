/**
 * @Author hbs
 * @Date 2019/7/26
 */
public interface Pool {

    void execute(Runnable runnable);
    void shutDownNow();
    void addWorker(Runnable runnable);
}
