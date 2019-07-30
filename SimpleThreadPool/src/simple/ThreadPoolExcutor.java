package simple;

import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author hbs
 * @Date 2019/7/26
 * 线程池的简单实现
 */
public class ThreadPoolExcutor implements Pool {
    //运行状态
    private volatile boolean RUNNING = true;
    //核心线程数
    private volatile int corePoolSize = 0;
    //工作线程数
    private AtomicInteger workerCount = new AtomicInteger(0);
    //最大线程数
    private volatile int maximumPoolSize = 0;
    private volatile RejectExecutionHandler handler;
    private final ReentrantLock mainLock = new ReentrantLock();

    //出队阻塞 take,入队不阻塞 offer
    private final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(3);
    private final HashSet<Worker> workers = new HashSet<>();

    public ThreadPoolExcutor(int corePoolSize,int maximumPoolSize){
        this.handler = new RejectExecutionHandler();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
    }


    /**
     * 任务执行方法
     * @param runnable
     */
    @Override
    public void execute(Runnable runnable) {
        if(runnable == null) throw new NullPointerException();
        System.out.println(workQueue.size() + "&&" + workerCount.get());
        if((workerCount.get()>=corePoolSize)){
            if(!workQueue.offer(runnable) && workerCount.get()>=maximumPoolSize){
                reject();
            }else{
                if(workerCount.get()<maximumPoolSize) addWorker(runnable,false);
            }
        }else {
            addWorker(runnable,true);
        }

    }

    /**
     * 任务停止方法
     */
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


    /**
     * 拒绝策略直接抛出异常
     */
    public void reject(){
        handler.rejectedExecution();
    }

    /**
     * 创建工作线程，并添加任务
     * @param runnable
     */
    @Override
    public void addWorker(Runnable runnable,boolean core) {
        boolean workerAdded = false;
        final ReentrantLock mainLock = this.mainLock;
        Worker worker = new Worker(runnable);
        Thread t = worker.t;
        mainLock.lock();
        if(workerCount.get()<(core?corePoolSize:maximumPoolSize) && RUNNING ==true) {
            workerCount.getAndIncrement();
            workers.add(worker);
            workerAdded = true;
        }
        mainLock.unlock();
        if(workerAdded) {
            t.start();
        }
    }

    /**
     * 获取队列任务
     * @return
     */
    public Runnable getTask() {
        try {
            return  workQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 任务对象
     */
    class Worker implements Runnable{
        Thread t;
        private Runnable firstTask;
        private final ReentrantLock runlock = new ReentrantLock();
        volatile long completedTasks;
        public Worker(Runnable firstTask){

            this.firstTask = firstTask;
            t = ThreadFactory.newThread(this);
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
            final ReentrantLock runLock = this.runlock;
            runLock.lock();
            task.run();
            completedTasks++;
            runLock.unlock();
        }

    }

}
