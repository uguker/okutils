package com.uguke.android.okgo;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpMethod;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.DeleteRequest;
import com.lzy.okgo.request.OptionsRequest;
import com.lzy.okgo.request.PatchRequest;
import com.lzy.okgo.request.PostRequest;
import com.lzy.okgo.request.PutRequest;
import com.lzy.okgo.request.base.Request;
import com.uguke.android.okgo.Callback;
import com.uguke.android.okgo.LoadingDialog;
import com.uguke.android.okgo.NetData;
import com.uguke.reflect.TypeBuilder;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;

public class OkRequest<T> {

    /** NetData内嵌Object **/
    static final int TYPE_NET_OBJECT = 0;
    /** NetData内嵌List **/
    static final int TYPE_NET_LIST = 1;
    /** String数据 **/
    static final int TYPE_STRING = 2;
    /** String数据 **/
    static final int TYPE_FILE = 3;
    // OkGo请求相关

    private String mUrl;
    private String mUpJson;
    /** 网络请求返回实体的Class **/
    private Type mType;
    private Object mTag;
    private boolean mDownload;
    /** 是否加在过滤规则中的数据以onSucceed()返回 **/
    private boolean mToSucceed;

    private List<Integer> mFilters;
    private HttpMethod mMethod;
    private HttpParams mParams;
    private HttpHeaders mHeaders;


    private int mRequestType;
    // Loading对话框相关

    private int mLoadingColor;
    private float mLoadingSize;
    private String mLoadingText;
    private boolean mLoadingDimEnable;
    private LoadingDialog mLoading;

    private Object mExtra;
    private FiltersHandler mFiltersHandler;
    private HeadersHandler mHeadersHandler;

    /** 用来防止空指针 **/
    private Reference<Object> mReference;

    OkRequest(Object obj, Class<?> clazz, int type) {
        mRequestType = type;
        mReference = new WeakReference<>(obj);
        switch (type) {
            case TYPE_NET_OBJECT:
                mType = TypeBuilder.newInstance(NetDataImpl.class)
                        .addTypeParam(clazz)
                        .build();
                break;
            case TYPE_NET_LIST:
                mType =  TypeBuilder.newInstance(NetDataImpl.class)
                        .beginSubType(List.class)
                        .addTypeParam(clazz)
                        .endSubType()
                        .build();
                break;
            case TYPE_STRING:
                mType = TypeBuilder.newInstance(String.class).build();
                break;
            case TYPE_FILE:
                mType = TypeBuilder.newInstance(File.class).build();
                break;
            default:
        }
        mParams = new HttpParams();
        mHeaders = new HttpHeaders();
        mFilters = new LinkedList<>();
    }

    //================================================//
    //=================接口请求方式===================//
    //================================================//

    public OkRequest<T> get(String url) {
        mUrl = url;
        mMethod = HttpMethod.GET;
        return this;
    }

    public OkRequest<T> post(String url) {
        mUrl = url;
        mMethod = HttpMethod.POST;
        return this;
    }

    public OkRequest<T> put(String url) {
        mUrl = url;
        mMethod = HttpMethod.PUT;
        return this;
    }

    public OkRequest<T> delete(String url) {
        mUrl = url;
        mMethod = HttpMethod.DELETE;
        return this;
    }

    public OkRequest<T> head(String url) {
        mUrl = url;
        mMethod = HttpMethod.HEAD;
        return this;
    }

    public OkRequest<T> patch(String url) {
        mUrl = url;
        mMethod = HttpMethod.PATCH;
        return this;
    }

    public OkRequest<T> options(String url) {
        mUrl = url;
        mMethod = HttpMethod.OPTIONS;
        return this;
    }

    public OkRequest<T> trace(String url) {
        mUrl = url;
        mMethod = HttpMethod.TRACE;
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
        mParams.put(key, value, replace);
        return this;
    }

    public OkRequest<T> params(String key, int value, boolean... replace) {
        mParams.put(key, value, replace);
        return this;
    }

    public OkRequest<T> params(String key, float value, boolean... replace) {
        mParams.put(key, value, replace);
        return this;
    }

    public OkRequest<T> params(String key, double value, boolean... replace) {
        mParams.put(key, value, replace);
        return this;
    }

    public OkRequest<T> params(String key, long value, boolean... replace) {
        mParams.put(key, value, replace);
        return this;
    }

    public OkRequest<T> params(String key, char value, boolean... replace) {
        mParams.put(key, value, replace);
        return this;
    }

    public OkRequest<T> params(String key, boolean value, boolean... replace) {
        mParams.put(key, value, replace);
        return this;
    }

    public OkRequest<T> params(Map<String, String> params, boolean... replace) {
        mParams.put(params, replace);
        return this;
    }

    public OkRequest<T> params(String key, File file) {
        mParams.put(key, file);
        return this;
    }

    public OkRequest<T> params(String key, List<File> files) {
        mParams.putFileParams(key, files);
        return this;
    }

    public OkRequest<T> paramsWrapper(String key, HttpParams.FileWrapper fileWrapper) {
        mParams.put(key, fileWrapper);
        return this;
    }

    public OkRequest<T> paramsWrapper(String key, List<HttpParams.FileWrapper> fileWrappers) {
        mParams.putFileWrapperParams(key, fileWrappers);
        return this;
    }

    public OkRequest<T> addUrlParams(String key, List<String> values) {
        mParams.putUrlParams(key, values);
        return this;
    }

    public OkRequest<T> removeParam(String key) {
        mParams.remove(key);
        return this;
    }

    public OkRequest<T> removeAllParams() {
        mParams.clear();
        return this;
    }

    public void execute(Callback<NetData<T>> callback) {
        // 如果是请求字符串
        if (mRequestType == TYPE_STRING) {
            Request<String, ?> request = OkGo.get(mUrl);
            request.tag(mTag);
            request.params(mParams);
            request.headers(mHeaders);
            executeForString(request, callback);
            return;
        }
        // 如果是请求文件
        if (mRequestType == TYPE_FILE) {
            Request<File, ?> request = OkGo.get(mUrl);
            request.tag(mTag);
            request.params(mParams);
            request.headers(mHeaders);
            executeForFile(request, callback);
            return;
        }
        // 其他正常网络请求
        Request<String, ?> request;
        switch (mMethod) {
            case POST:
                PostRequest<String> postRequest = OkGo.post(mUrl);
                if (!TextUtils.isEmpty(mUpJson)) {
                    postRequest.upJson(mUpJson);
                }
                request = postRequest;
                break;
            case PUT:
                PutRequest<String> putRequest = OkGo.put(mUrl);
                if (!TextUtils.isEmpty(mUpJson)) {
                    putRequest.upJson(mUpJson);
                }
                request = putRequest;
                break;
            case DELETE:
                DeleteRequest<String> deleteRequest = OkGo.delete(mUrl);
                if (!TextUtils.isEmpty(mUpJson)) {
                    deleteRequest.upJson(mUpJson);
                }
                request = deleteRequest;
                break;
            case HEAD:
                request = OkGo.head(mUrl);
                break;
            case PATCH:
                PatchRequest<String> patchRequest = OkGo.patch(mUrl);
                if (!TextUtils.isEmpty(mUpJson)) {
                    patchRequest.upJson(mUpJson);
                }
                request = patchRequest;
                break;
            case OPTIONS:
                OptionsRequest<String> optionsRequest = OkGo.options(mUrl);
                if (!TextUtils.isEmpty(mUpJson)) {
                    optionsRequest.upJson(mUpJson);
                }
                request = optionsRequest;
                break;
            case TRACE:
                request = OkGo.trace(mUrl);
                break;
            default:
                request = OkGo.get(mUrl);
        }
        request.tag(mTag);
        request.params(mParams);
        request.headers(mHeaders);
        executeForNet(request, callback);
    }

    private void executeForFile(final Request<File, ?> request, final Callback<NetData<T>> callback) {

    }

    private void executeForNet(final Request<String, ?> request, final Callback<NetData<T>> callback) {

    }

    private void executeForString(final Request<String, ?> request, final Callback<NetData<T>> callback) {
        request.execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                // 如果需要，取消对话框
                dismissLoading();
                String body = response.body();
                Headers headers = response.headers();

                // 如果处理Headers进行了拦截
                if (handleHeaders(headers)) {
                    callback.onFailed("");
                    return;
                }
//                OkUtils utils = OkUtils.Holder.INSTANCE;

                // 根据code回调
//                if (utils.mSucceedCode == data.getCode()) {
//                    callback.onSucceed(data);
//                } else if (utils.mFailedCode == data.getCode()) {
//                    callback.onFailed(data.getMessage());
//                } else {
//                    for (int filter : mFilters) {
//                        if (filter == data.getCode()) {
//                            handleFilters(data, mToSucceed, callback);
//                            return;
//                        }
//                    }
//                    handleFilters(data, !mToSucceed, callback);
//                }

                NetData<T> data;
                try {
                    data = new Gson().fromJson(response.body(), mType);
                } catch (JsonParseException e) {
                    // Json数据错误
                    data = null;
                }
            }

            @Override
            public void onStart(Request<String, ? extends Request> request) {
                super.onStart(request);
                showLoading();
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                dismissLoading();
                ///callback.onFailed(response);
                //callback.onFailed(OkUtils.Holder.INSTANCE.mFailedText);
            }
        });
    }

    private boolean handleHeaders(Headers headers) {
        OkUtils utils = OkUtils.Holder.INSTANCE;
        // 如果设置了Headers处理器
        if (mHeadersHandler != null || utils.mHeadersHandler != null) {
            if (mHeadersHandler != null) {
                // 不继续向下
                return mHeadersHandler.onHandle(headers, mExtra);
            } else {
                // 不继续向下
                return utils.mHeadersHandler.onHandle(headers, mExtra);
            }
        }
        return false;
    }

    /**
     * 功能描述：关联的目标文件是否被释放（就是防止空指针）
     * @return true 被释放
     */
    private boolean isReleased() {
        return mReference == null || mReference.get() == null;
    }


    private void showLoading() {

    }

    private void dismissLoading() {
        
    }
}