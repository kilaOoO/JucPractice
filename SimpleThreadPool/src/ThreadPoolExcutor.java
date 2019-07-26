import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author hbs
 * @Date 2019/7/26
 */
public class ThreadPoolExcutor implements Pool {
    private volatile boolean RUNNING = true;
    private volatile int corePoolSize = 0;
    private volatile int poolSize = 0;
    private final ReentrantLock mainLock = new ReentrantLock();

    private final BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<>();
    private final HashSet<Worker> workers = new HashSet<>();

    public ThreadPoolExcutor(int poolSize,int corePoolSize){
        this.poolSize =poolSize;
        this.corePoolSize = corePoolSize;
    }


    @Override
    public void execute(Runnable runnable) {
        if(runnable == null) throw new NullPointerException();
        if(poolSize>=corePoolSize){
            try {
                workQueue.put(runnable);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else {
            addWorker(runnable);
        }

    }

    @Override
    public void shutDownNow() {
        RUNNING = false;
        if(!workers.isEmpty()){
            for(Worker worker:workers){
                worker.t.interrupt();
            }
        }
        Thread.currentThread().interrupt();
    }

    @Override
    public void addWorker(Runnable runnable) {
        final ReentrantLock mainLock = this.mainLock;
        Thread t = null;
        mainLock.lock();
        if(poolSize<corePoolSize && RUNNING ==true) {
            poolSize++;
            Worker worker = new Worker(runnable);
            workers.add(worker);
            t = new Thread(worker);
            worker.t = t;
        }
        mainLock.unlock();
        if(t!=null) {
            t.start();
        }
    }

    public Runnable getTask() {
        try {
            return  workQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    class Worker implements Runnable{
        Thread t;
        private Runnable firstTask;
        private final ReentrantLock runlock = new ReentrantLock();
        volatile long completedTasks;
        public Worker(Runnable firstTask){
            this.firstTask = firstTask;
        }


        @Override
        public void run() {
            Runnable task = firstTask;
            while(RUNNING && (task!=null || (task = getTask())!=null)){
                runTask(task);
                task = null;

            }
        }

        public void runTask(Runnable task){
            //final ReentrantLock runLock = this.runlock;
            //runLock.lock();
            task.run();
            completedTasks++;
            //runLock.unlock();
        }

    }

}
