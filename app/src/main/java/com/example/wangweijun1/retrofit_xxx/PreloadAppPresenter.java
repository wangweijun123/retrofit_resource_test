
package com.example.wangweijun1.retrofit_xxx;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Integer.parseInt;

/**
 * Created by zhangguanhua on 16-12-8.
 */

public class PreloadAppPresenter {

    private static final String ACTION_LETV_STORE_QUIET_INSTALL_FAILED = "com.letv.tvos.appstore.installAPKFailure";

    private static final String TAG = PreloadAppPresenter.class.getSimpleName();
    private static final Uri OBSERVER_STORE_URI = Uri.parse("content://com.letv.tvos.appstore.downloadprovider.download/my_downloads");
    private static final Uri QUERY_STORE_URI = Uri.parse("content://com.letv.tvos.appstore.downloadprovider.download/all_downloads");

    private static final String ID_FOR_STORE = "_id";
    private static final String STATUS_FOR_STORE = "status";
    private static final String USER_ID_FOR_STORE = "user_id";
    private static final String TOTAL_BYTES_FOR_STORE = "total_bytes";
    private static final String CURRENT_BYTES_FOR_STORE = "current_bytes";
    private static final String SORT_ORDER = "_id desc";

    /**
     * This download hasn't stated yet
     *
     * @hide
     */
    private static final int STATUS_PENDING = 190;

    /**
     * waiting for download
     */
    private static final int STATUS_WAITING_FOR_DOWNLOAD = 191;

    /**
     * This download has started
     *
     * @hide
     */
    private static final int STATUS_RUNNING = 192;

    /**
     * This download has been paused by the owning app.
     */
    private static final int STATUS_PAUSED_BY_APP = 193;

    /**
     * This download encountered some network error and is waiting before retrying the request.
     */
    private static final int STATUS_WAITING_TO_RETRY = 194;

    /**
     * This download is waiting for network connectivity to proceed.
     */
    private static final int STATUS_WAITING_FOR_NETWORK = 195;

    /**
     * This download exceeded a size limit for mobile networks and is waiting for a Wi-Fi connection to proceed.
     */
    private static final int STATUS_QUEUED_FOR_WIFI = 196;

    /**
     * This download has successfully completed. Warning: there might be other status values that indicate success in the future. Use isSucccess() to capture the entire category.
     */
    private static final int STATUS_SUCCESS = 200;

    /**
     * This request couldn't be parsed. This is also used when processing requests with unknown/unsupported URI schemes.
     *
     * @hide
     */
    private static final int STATUS_BAD_REQUEST = 400;

    /**
     * This download was canceled
     */
    private static final int STATUS_CANCELED = 490;

    private static final String SPLITE_STR = "_________";

    private StoreStateContentObserver observer;
    private Context mContext;
    private static PreloadAppPresenter INSTANCE;
    private Handler mHandler;
    private Handler mQueryStoreHandler;
    private HandlerThread mHandlerThread;

    public static PreloadAppPresenter getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new PreloadAppPresenter(context);
        }
        return INSTANCE;
    }

    private PreloadAppPresenter(Context context) {
        this.mContext = context;
        mHandler = new Handler();
    }

    public void registerObserverForStore() {
        try {
            init();
            mContext.getContentResolver().registerContentObserver(OBSERVER_STORE_URI, true, observer);
            registerInstallReceiver();
            Log.i(TAG, "registerObserverForStore success");
        } catch (Exception ex) {
            Log.d(TAG, "registerObserverForStore error!!!", ex);
        }
    }

    public void registerInstallReceiver() {
        InstallReceiver installReceiver = new InstallReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(ACTION_LETV_STORE_QUIET_INSTALL_FAILED);
        intentFilter.addDataScheme("package");
        mContext.registerReceiver(installReceiver, intentFilter);
    }

    private class InstallReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action:"+action);
            if (ACTION_LETV_STORE_QUIET_INSTALL_FAILED.equalsIgnoreCase(action)) {
                String packageName = intent.getStringExtra("packageName");
                Log.d(TAG, "packageName:"+packageName);
            } else if (Intent.ACTION_PACKAGE_REMOVED.equalsIgnoreCase(action)){
                String packageName = intent.getDataString();
                if (packageName != null && packageName.contains(":")) {
                    packageName = packageName.substring(packageName.indexOf(":") + 1);
                }
                Log.d(TAG, "packageName:"+packageName);
            } else {

            }


        }
    }


    private void init(){
        this.observer = new StoreStateContentObserver(mHandler);
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mQueryStoreHandler = new Handler(mHandlerThread.getLooper());
    }

    public void unRegisterObserverForStore() {
        try {
            if (observer != null) {
                mContext.getContentResolver().unregisterContentObserver(observer);
                Log.d(TAG, "unregisterObserverForStore success");
                observer = null;
            }
        } catch (Exception ex) {
            Log.d(TAG, "unregisterObserverForStore error !!!", ex);
        }
    }

    private class StoreStateContentObserver extends ContentObserver {

        /**
         * onChange() will happen on the provider Handler.
         *
         * @param handler The handler to run {@link #onChange} on.
         */
        public StoreStateContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // Log.d(TAG, "onChange=====");
            // queryStateFormStore();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

            try {
                long id = ContentUris.parseId(uri);
                final Uri queryUri = ContentUris.withAppendedId(QUERY_STORE_URI, id);
                 Log.d(TAG, "onChange queryUri = " + queryUri+", uri" + uri+", thread id:"+Thread.currentThread().getId());
                mQueryStoreHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        queryStateFromStore(queryUri);
                    }
                });
            } catch (Exception ex) {
                //Log.d(TAG, "onChange parseId error", ex);
            }
        }
    }

    private void runOnUiThread(Runnable r) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            r.run();
        } else {
            mHandler.post(r);
        }
    }

    private void queryStateFromStore(final Uri uri) {
        Log.d(TAG, "queryStateFromStore  thread id:"+Thread.currentThread().getId());
        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(uri, new String[] {
                    ID_FOR_STORE, STATUS_FOR_STORE, USER_ID_FOR_STORE, CURRENT_BYTES_FOR_STORE,
                    TOTAL_BYTES_FOR_STORE
            }, null, null, SORT_ORDER);
            if (cursor == null) {
                return;
            }
            // Log.d(TAG, "queryStateFormStore onchange count = " + cursor.getCount());
            while (cursor.moveToNext()) {
                String userId = cursor.getString(cursor.getColumnIndex(USER_ID_FOR_STORE));
                String packageName = getPackageNameFromUserId(userId);
                    int id = cursor.getInt(cursor.getColumnIndex(ID_FOR_STORE));
                    int status = cursor.getInt(cursor.getColumnIndex(STATUS_FOR_STORE));
                    long totalBytes = cursor.getLong(cursor.getColumnIndex(TOTAL_BYTES_FOR_STORE));
                    long currentBytes = cursor.getLong(cursor.getColumnIndex(CURRENT_BYTES_FOR_STORE));
                    Log.d(TAG, "queryStateFromStore onChange packagename = " + packageName + " status = "
                            + status + " totalBytes = " + totalBytes + " currentBytes = " + currentBytes
                            + " userId = " + userId + " _id = " + id);

            }
        } catch (Exception ex) {
            Log.d(TAG, "query store db error", ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    private String getPackageNameFromUserId(String userId) {
        if (!TextUtils.isEmpty(userId) && userId.contains(SPLITE_STR)) {
            String[] splitStr = userId.split(SPLITE_STR);
            if (splitStr != null && splitStr.length > 0) {
                return splitStr[0];
            }
        }
        return null;
    }

    /**
     * Returns whether the status is a success (i.e. 2xx).
     */
    public boolean isStatusSuccess(int status) {
        return (status >= 200 && status < 300);
    }

    /**
     * Returns whether the status is an error (i.e. 4xx or 5xx).
     */
    public boolean isStatusError(int status) {
        return (status >= 400 && status < 600);
    }

    /**
     * Returns whether the status is informational (i.e. 1xx).
     */
    public boolean isStatusInformational(int status) {
        return (status >= 100 && status < 200);
    }

    /**
     * Returns whether the status is pending
     *
     * @param status
     * @param currentBytes
     * @return
     */
    public boolean isStatusPending(int status, long currentBytes) {
        return ((status == STATUS_RUNNING) || (status == STATUS_WAITING_FOR_DOWNLOAD)) && (currentBytes == 0);
    }

    /**
     * Returns whether the status is loading
     *
     * @param status
     * @param currentBytes
     * @return
     */
    public boolean isStausLoading(int status, long currentBytes) {
        return status == STATUS_RUNNING && (currentBytes != 0);
    }

    public boolean isShowShade(String statusStr) {
        if (statusStr == null) {
            return false;
        }
        try {
            int status = parseInt(statusStr);
            return isStatusInformational(status) || isStatusSuccess(status);
        } catch (Exception ex) {
            Log.d(TAG, "isShowShade exception", ex);
        }
        return false;
    }
}
