package top.amosen.asyncSchedule.result;

import com.sun.istack.internal.Nullable;
import sun.misc.Unsafe;
import top.amosen.asyncSchedule.exception.AResultConflictException;

import java.lang.reflect.Field;

/**
 * 一个AWorkerResult实例代表了一个异步任务的操作结果，
 * 它可以被单个线程写，多个线程读
 *
 * @author Amosen
 * @Date 2023-03-15 17:24
 */
public class AWorkerResult {

    private static final Object NONE_RESULT = new Object();

    @Nullable
    private Object result;

    private Throwable throwable;

    public AWorkerResult() {
        this.result = NONE_RESULT;
    }

    public boolean hasResult() {
        return result != NONE_RESULT;
    }

    public boolean hasError() {
        return throwable != null;
    }

    public void setResult(Object result) {
        boolean success = false;
        try {
            long offset = unsafe().objectFieldOffset(this.getClass().getDeclaredField("result"));
            success = unsafe().compareAndSwapObject(this, offset, NONE_RESULT, result);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        if (!success) {
            throwConflictException("an attempt was made to write data to a result that has already been written");
        }
    }

    public void setError(Throwable throwable) {
        boolean success = false;
        try {
            long offset = unsafe().objectFieldOffset(this.getClass().getDeclaredField("throwable"));
            success = unsafe().compareAndSwapObject(this, offset, null, throwable);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        if (!success) {
            throwConflictException("an attempt was made to write data to a result that has already been written");
        }
    }

    public Object getResult() {
        return result;
    }

    public Throwable getError() {
        return throwable;
    }

    private Unsafe unsafe() {
        try {
            Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private void throwConflictException(String msg) {
        throw new AResultConflictException(msg);
    }

}
