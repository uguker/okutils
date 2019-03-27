package com.uguke.android.okgo;

import android.app.Fragment;
import android.support.annotation.ColorInt;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpMethod;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Progress;

import com.lzy.okgo.request.base.BodyRequest;
import com.lzy.okgo.request.base.Request;
import com.uguke.reflect.TypeBuilder;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;

/**
 * OkUtils网络请求
 * @author LeiJue
 */
public class OkRequest<T> {

    /** NetData内嵌Object **/
    static final int TYPE_NET_OBJECT = 0;
    /** NetData内嵌List **/
    static final int TYPE_NET_LIST = 1;
    /** String数据 **/
    static final int TYPE_STRING = 2;
    /** String数据 **/
    static final int TYPE_FILE = 3;

    static final String TAG_DIALOG = "tag_dialog";
    // OkGo请求相关
    private String requestUrl;
    private String mUpJson;

    private Object mTag;
    private FragmentActivity activity;
    private HttpMethod httpMethod;
    private HttpParams httpParams;
    private HttpHeaders httpHeaders;
    
    private Type responseType;
    private Class<?> responseDataClass;

    private int requestType;
    // Loading对话框相关

    private int mLoadingColor;
    private float mLoadingSize;
    private String mLoadingText;
    private boolean mLoadingDimEnable;
    private LoadingDialog mLoading;

    private SparseBooleanArray responseCodes;
    private ConvertHandler convertHandler;
    private EncryptHandler encryptHandler;
    private HeadersInterceptor headersInterceptor;

    /** 用来防止空指针 **/
    private Reference<Object> reference;

    OkRequest(Object obj, Class<?> clazz, int type) {
        requestType = type;
        responseDataClass = clazz;
        reference = new WeakReference<>(obj);
        OkUtils okUtils = OkUtils.getInstance();
        switch (type) {
            case TYPE_NET_OBJECT:
                responseType = TypeBuilder.newInstance(okUtils.getResponseClass())
                        .addTypeParam(clazz)
                        .build();
                break;
            case TYPE_NET_LIST:
                responseType =  TypeBuilder.newInstance(okUtils.getResponseClass())
                        .beginSubType(List.class)
                        .addTypeParam(clazz)
                        .endSubType()
                        .build();
                break;
            case TYPE_STRING:
                responseType = TypeBuilder.newInstance(String.class).build();
                break;
            case TYPE_FILE:
                responseType = TypeBuilder.newInstance(File.class).build();
                break;
            default:
        }
        httpParams = new HttpParams();
        httpHeaders = new HttpHeaders();
        responseCodes = new SparseBooleanArray();
        responseCodes.put(okUtils.getSucceedCode(), true);
        responseCodes.put(okUtils.getFailedCode(), false);
    }

    //================================================//
    //=================接口请求方式===================//
    //================================================//

    public OkRequest<T> get(String url) {
        requestUrl = url;
        httpMethod = HttpMethod.GET;
        return this;
    }

    public OkRequest<T> post(String url) {
        requestUrl = url;
        httpMethod = HttpMethod.POST;
        return this;
    }

    public OkRequest<T> put(String url) {
        requestUrl = url;
        httpMethod = HttpMethod.PUT;
        return this;
    }

    public OkRequest<T> delete(String url) {
        requestUrl = url;
        httpMethod = HttpMethod.DELETE;
        return this;
    }

    public OkRequest<T> head(String url) {
        requestUrl = url;
        httpMethod = HttpMethod.HEAD;
        return this;
    }

    public OkRequest<T> patch(String url) {
        requestUrl = url;
        httpMethod = HttpMethod.PATCH;
        return this;
    }

    public OkRequest<T> options(String url) {
        requestUrl = url;
        httpMethod = HttpMethod.OPTIONS;
        return this;
    }

    public OkRequest<T> trace(String url) {
        requestUrl = url;
        httpMethod = HttpMethod.TRACE;
        return this;
    }

    public OkRequest<T> upJson(String upJson) {
        mUpJson = upJson;
        return this;
    }


    //================================================//
    //=================接口参数设置===================//
    //================================================//

    public OkRequest<T> params(String key, String value, boolean... replace) {
        httpParams.put(key, value, replace);
        return this;
    }

    public OkRequest<T> params(String key, int value, boolean... replace) {
        httpParams.put(key, value, replace);
        return this;
    }

    public OkRequest<T> params(String key, float value, boolean... replace) {
        httpParams.put(key, value, replace);
        return this;
    }

    public OkRequest<T> params(String key, double value, boolean... replace) {
        httpParams.put(key, value, replace);
        return this;
    }

    public OkRequest<T> params(String key, long value, boolean... replace) {
        httpParams.put(key, value, replace);
        return this;
    }

    public OkRequest<T> params(String key, char value, boolean... replace) {
        httpParams.put(key, value, replace);
        return this;
    }

    public OkRequest<T> params(String key, boolean value, boolean... replace) {
        httpParams.put(key, value, replace);
        return this;
    }

    public OkRequest<T> params(Map<String, String> params, boolean... replace) {
        httpParams.put(params, replace);
        return this;
    }

    public OkRequest<T> params(String key, File file) {
        httpParams.put(key, file);
        return this;
    }

    public OkRequest<T> params(String key, List<File> files) {
        httpParams.putFileParams(key, files);
        return this;
    }

    public OkRequest<T> paramsWrapper(String key, HttpParams.FileWrapper fileWrapper) {
        httpParams.put(key, fileWrapper);
        return this;
    }

    public OkRequest<T> paramsWrapper(String key, List<HttpParams.FileWrapper> fileWrappers) {
        httpParams.putFileWrapperParams(key, fileWrappers);
        return this;
    }

    public OkRequest<T> addUrlParams(String key, List<String> values) {
        httpParams.putUrlParams(key, values);
        return this;
    }

    public OkRequest<T> removeParam(String key) {
        httpParams.remove(key);
        return this;
    }

    public OkRequest<T> removeAllParams() {
        httpParams.clear();
        return this;
    }

    public OkRequest<T> succeedCodes(int ... codes) {
        for (int code : codes) {
            responseCodes.put(code, true);
        }
        return this;
    }

    public OkRequest<T> failedCodes(int ... codes) {
        for (int code : codes) {
            responseCodes.put(code, false);
        }
        return this;
    }

    public OkRequest<T> convertHandler(ConvertHandler handler) {
        convertHandler = handler;
        return this;
    }

    public OkRequest<T> encryptHandler(EncryptHandler handler) {
        encryptHandler = handler;
        return this;
    }

    public OkRequest<T> headersInterceptor(HeadersInterceptor interceptor) {
        headersInterceptor = interceptor;
        return this;
    }

    @SuppressWarnings("unchecked")
    public void execute(Callback<Response<T>> callback) {
        // 如果是请求文件
        if (requestType == TYPE_FILE) {
            Request<File, ?> request = OkGo.get(requestUrl);
            request.tag(mTag);
            request.params(httpParams);
            request.headers(httpHeaders);
            executeForFile(request, callback);
            return;
        }
        // 其他正常网络请求
        Request<String, ?> request;
        switch (httpMethod) {
            case POST:
                request = OkGo.post(requestUrl);
                break;
            case PUT:
                request = OkGo.put(requestUrl);
                break;
            case DELETE:
                request = OkGo.delete(requestUrl);
                break;
            case HEAD:
                request = OkGo.head(requestUrl);
                break;
            case PATCH:
                request = OkGo.patch(requestUrl);
                break;
            case OPTIONS:
                request = OkGo.options(requestUrl);
                break;
            case TRACE:
                request = OkGo.trace(requestUrl);
                break;
            default:
                request = OkGo.get(requestUrl);
        }
        upData(request);
        request.tag(mTag);
        request.headers(httpHeaders);
        request.params(handleEncrypt(httpParams));
        executeForCommon(request, callback);
    }

    public OkRequest<T> showLoading(FragmentActivity activity) {
        mLoading = new LoadingDialog();
        this.activity = activity;
        return this;
    }

    public OkRequest<T> showLoading(Fragment fragment) {
        mLoading = new LoadingDialog();
        activity = (FragmentActivity) fragment.getActivity();
        return this;
    }

    public OkRequest<T> showLoading(View view) {
        mLoading = new LoadingDialog();
        activity = (FragmentActivity) view.getContext();
        return this;
    }

    public OkRequest<T> loadingColor(@ColorInt int color) {

        return this;
    }

    public OkRequest<T> loadingSize(float size) {

        return this;
    }

    public OkRequest<T> loadingTextColor(@ColorInt int color) {

        return this;
    }

    public OkRequest<T> loadingTextSize(float size) {
        return this;
    }

    public OkRequest<T> loadingDimEnable(boolean enable) {

        return this;
    }

    @SuppressWarnings("unchecked")
    private void upData(Request<String, ?> request) {
        if (!TextUtils.isEmpty(mUpJson) && request instanceof BodyRequest) {
            ((BodyRequest<String, BodyRequest>) request).upJson(mUpJson);
        }
    }

    private void executeForFile(final Request<File, ?> request, final Callback<Response<T>> callback) {
        request.execute(new FileCallback() {
            @Override
            public void onSuccess(com.lzy.okgo.model.Response<File> response) {
                if (isReleased()) {
                    return;
                }
                dismissLoading();
                callback.onSucceed(ResponseFactory.<T>createDownloadComplete(response.body()));
            }

            @Override
            public void onStart(Request<File, ?> request) {
                super.onStart(request);
                if (isReleased()) {
                    return;
                }
                showLoading();
            }

            @Override
            public void onError(com.lzy.okgo.model.Response<File> response) {
                super.onError(response);
                if (isReleased()) {
                    return;
                }
                dismissLoading();
                callback.onFailed(null);
            }

            @Override
            public void downloadProgress(Progress progress) {
                super.downloadProgress(progress);
                if (isReleased()) {
                    return;
                }
                callback.onProgress(progress);
            }
        });
    }

    private void executeForCommon(final Request<String, ?> request, final Callback<Response<T>> callback) {
        request.execute(new StringCallback() {
            @Override
            public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                if (isReleased()) {
                    return;
                }
                dismissLoading();
                String body = response.body();
                if (requestType == TYPE_STRING) {
                    callback.onSucceed(ResponseFactory.<T>createResponseBody(body));
                    return;
                }
                body = handleConvert(body);
                Headers headers = response.headers();
                if (interceptHeaders(headers)) {
                    callback.onFailed(ResponseFactory.<T>createHandledHeaders());
                    return;
                }
                Response<T> resultResponse;
                try {
                    resultResponse = new Gson().fromJson(body, responseType);
                } catch (JsonParseException e) {
                    callback.onFailed(ResponseFactory.<T>createParseJsonException());
                    return;
                }
                if (resultResponse == null) {
                    callback.onFailed(ResponseFactory.<T>createEmptyResponse());
                    return;
                }
                if (interceptResponse(resultResponse)) {
                    callback.onFailed(ResponseFactory.<T>createInterceptResponse());
                    return;
                }
                if (isSucceedResponse(resultResponse)) {
                    callback.onSucceed(resultResponse);
                } else {
                    callback.onFailed(resultResponse);
                }
            }

            @Override
            public void onStart(Request<String, ?> request) {
                super.onStart(request);
                if (isReleased()) {
                    return;
                }
                showLoading();
            }

            @Override
            public void onError(com.lzy.okgo.model.Response<String> response) {
                super.onError(response);
                if (isReleased()) {
                    return;
                }
                dismissLoading();
                switch (response.code()) {
                    case -1:
                        callback.onFailed(ResponseFactory.<T>createNoNetwork());
                        break;
                    default:
                }
            }
        });
    }

    private String handleConvert(String body) {
        ConvertHandler defaultConvertHandler = OkUtils.getInstance().getConvertHandler();
        ConvertHandler validHandler = convertHandler == null ? defaultConvertHandler : convertHandler;
        if (validHandler != null) {
            return validHandler.onHandle(body);
        }
        return body;
    }

    private HttpParams handleEncrypt(HttpParams params) {
        EncryptHandler defaultHandler = OkUtils.getInstance().getEncryptHandler();
        EncryptHandler validHandler = encryptHandler == null ? defaultHandler : encryptHandler;
        return validHandler == null ? params : validHandler.onHandle(params);
    }

    private boolean interceptHeaders(Headers headers) {
        HeadersInterceptor defaultInterceptor = OkUtils.getInstance().getHeadersInterceptor();
        HeadersInterceptor validInterceptor = headersInterceptor == null ? defaultInterceptor : headersInterceptor;
        return validInterceptor != null && validInterceptor.onIntercept(headers);
    }

    private boolean interceptResponse(Response<T> response) {
        ResponseInterceptor defaultInterceptor = OkUtils.getInstance().getResponseInterceptor();
        return defaultInterceptor != null && defaultInterceptor.onIntercept(response);
    }

    private boolean isSucceedResponse(Response<T> response) {
        int len = responseCodes.size();
        for (int i = 0; i < len; i++) {
            int code = responseCodes.keyAt(i);
            if (code == response.code()) {
                return responseCodes.valueAt(i);
            }
        }
        return false;
    }

    private boolean isReleased() {
        return reference == null || reference.get() == null;
    }

    private void showLoading() {
        if (mLoading != null) {
            OkUtils utils = OkUtils.Holder.INSTANCE;
//            if (utils.mLoadingColor == 0) {
//                utils.mLoadingColor = ContextCompat.getColor(
//                        OkGo.getInstance().getContext(), R.color.colorAccent);
//            }


            mLoadingText = TextUtils.isEmpty(mLoadingText) ? utils.mLoadingText : mLoadingText;
            mLoadingSize = mLoadingSize == 0 ? utils.mLoadingSize : mLoadingSize;
            mLoading
                    //.text(mLoadingText)
                    //.size(mLoadingSize)
                    //.color(mLoadingColor)
            //        .dimEnable(mLoadingDimEnable)
                    .show(activity.getSupportFragmentManager(), TAG_DIALOG);
        }
    }

    private void dismissLoading() {

        FragmentManager manager = activity.getSupportFragmentManager();

        LoadingDialog dialog = (LoadingDialog) manager.findFragmentByTag(TAG_DIALOG);
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
