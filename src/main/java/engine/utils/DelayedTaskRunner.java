package engine.utils;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DelayedTaskRunner {
	
	private static class DelayedTask {
	    private final Callable<Void> runnable;
	    private int delay;

	    public DelayedTask(Callable<Void> runnable, int delay) {
	        this.runnable = runnable;
	        this.delay = delay;
	    }
	}
	
	private final Queue<DelayedTask> delayedTasks = new ConcurrentLinkedQueue<>();
	
	public void addTask(Callable<Void> task, int delay) {
		delayedTasks.add(new DelayedTask(task, delay));
	}
	
	public void drainTasks() {
        Iterator<DelayedTask> iter = delayedTasks.iterator();
        
        while (iter.hasNext()) {
        	
        	DelayedTask task = iter.next();
        	
            if (task.delay > 0) {
                task.delay--;
                continue;
            }
            
            try {
                iter.remove();
                task.runnable.call();
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }
	}
}