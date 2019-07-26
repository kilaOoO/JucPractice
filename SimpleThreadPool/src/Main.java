public class Main {

    public static void main(String[] args) {
        ThreadPoolExcutor excutor = new ThreadPoolExcutor(0,3);
        for(int i=0;i<10;i++){
            excutor.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("线程" + Thread.currentThread().getName());
                }
            });
        }
    }
}
