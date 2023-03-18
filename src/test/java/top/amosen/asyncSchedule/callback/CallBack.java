package top.amosen.asyncSchedule.callback;

import top.amosen.asyncSchedule.result.AWorkerResult;

import java.util.Map;

/**
 * @author Amosen
 * @Date 2023-03-16 15:37
 */
public class CallBack implements ACallback<String, String>{

    private String name;

    public CallBack(String name) {
        this.name = name;
    }

    @Override
    public void onBegin(String param) {
        System.out.println(name + " begin..." + param);
    }

    @Override
    public void onResult(String result) {
        System.out.println(name + " result..." + result);
    }

    @Override
    public boolean onError(Throwable throwable, String result) {
        throwable.printStackTrace();
        return false;
    }

    @Override
    public void onComplete(String param, String result, Throwable throwable) {
        System.out.println(name + " complete... param: " + param + " result: " + result);
        if (null != throwable) {
            throwable.printStackTrace();
        }

    }

    @Override
    public void onFail(Throwable throwable, Map<String, AWorkerResult> results) {

    }
}
