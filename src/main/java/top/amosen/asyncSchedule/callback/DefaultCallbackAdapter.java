package top.amosen.asyncSchedule.callback;

/**
 * @author Amosen
 * @Date 2023-03-16 13:54
 */
public class DefaultCallbackAdapter<P, R> implements ACallback<P, R>{
    @Override
    public void onBegin(Object param) {

    }

    @Override
    public void onResult(R result) {

    }

    @Override
    public boolean onError(Throwable throwable, R result) {
        return false;
    }

    @Override
    public void onComplete(P param, R result, Throwable throwable) {

    }
}
