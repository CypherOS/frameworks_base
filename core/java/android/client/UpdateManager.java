/**
 * Copyright (C) 2018 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.client;

import android.client.common.VersionManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that coordinates system update processes.
 * <p>
 * Use {@link android.content.Context#getSystemService(java.lang.String)}
 * with argument {@link android.content.Context#COTA_SERVICE} to get an
 * instance of this class.
 *
 * @author Chris Crump
 * @hide
 */
public class UpdateManager implements Response.Listener<JSONObject>, Response.ErrorListener {

    private static final String TAG = UpdateManager.class.getSimpleName();

    private Context mContext;
    private IUpdateService mService;

	private List<Listener> mListeners = new ArrayList<>();
    private UpdateInfo[] mLastUpdates = new UpdateInfo[0];

	private Client mClient;
	private Client[] mClients;
	private boolean mScanning = false;
	private boolean mClientWorks = false;
	private int mCurrentClient = -1;
	private RequestQueue mQueue;

    public UpdateManager(Context context, IUpdateService service) {
        mContext = context;
        mService = service;
        if (mService == null) {
            Slog.v(TAG, "UpdateService returned null");
        }
		mQueue = Volley.newRequestQueue(context);
    }
	
	public abstract VersionManager getVersion();

    public abstract String getDevice();
	
	public UpdateInfo[] getLastUpdates() {
        return mLastUpdates;
    }
	
	public void addListener(Listener listener) {
        mListeners.add(listener);
    }
	
	public void removeListener(Listener listener) {
        mListeners.remove(listener);
    }
	
	public void setLastUpdates(UpdateInfo[] infos) {
        if (infos == null) {
            infos = new UpdateInfo[0];
        }
        mLastUpdates = infos;
    }
	
	public void checkForUpdates() {
        if (mScanning) return;
		if (!isNetworkAvailable()) return;
        mClientWorks = false;
        mScanning = true;
		initStartChecking();
        checkClient();
    }

	private void checkClient() {
        mScanning = true;
        mCurrentClient++;
        mClient = mClients[mCurrentClient];
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(mClient.getUrl(
                getDevice(), getVersion()), null, this, this);
        mQueue.add(jsObjRequest);
    }
	
	protected void initStartChecking(final UpdateInfo[] info) {
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                public void run() {
					for (Listener listener : mListeners) {
                        listener.startChecking();
                    }
                }
            });
        }
    }

	protected void initCheckCompleted(final UpdateInfo[] info) {
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                public void run() {
					for (Listener listener : mListeners) {
                        listener.onUpdateFound(info);
                    }
                }
            });
        }
    }

    protected void initCheckError(final String reason) {
        if (mContext instanceof Activity) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                public void run() {
					for (Listener listener : mListeners) {
                        listener.onCheckError(reason);
                    }
                }
            });
        }
    }
	
	@Override
    public void onResponse(JSONObject response) {
        mScanning = false;
        try {
            UpdateInfo[] lastUpdates;
            setLastUpdates(null);
            List<UpdateInfo> list = mClient.createPackageInfoList(response);
            String error = mClient.getError();

            lastUpdates = list.toArray(new UpdateInfo[list.size()]);
            if (lastUpdates.length > 0) {
                mClientWorks = true;
                /** Add a notification here **/
            } else {
                if (error != null && !error.isEmpty()) {
                    if (isError(error)) {
                        return;
                    }
                } else {
                    mClientWorks = true;
                    if (mCurrentClient < mClients.length - 1) {
                        checkClient();
                        return;
                    }
                }
            }
            mCurrentClient = -1;
            setLastUpdates(lastUpdates);
            initCheckCompleted(lastUpdates);
        } catch (Exception ex) {
            ex.printStackTrace();
            isError(null);
        }
    }
	
	@Override
    public void onErrorResponse(VolleyError ex) {
        mScanning = false;
        isError(null);
    }
	
	private boolean isError(String error) {
        if (mCurrentClient < mClients.length - 1) {
            checkClient();
            return true;
        }
        mCurrentClient = -1;
        initCheckCompleted(null);
        initCheckError(error);
        return false;
    }
	
	private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
	
	public interface UpdateInfo extends Serializable {

        String getMd5();

        String getFilename();

        String getPath();

        String getHost();

        String getSize();
		
		String getText();

        VersionManager getVersion();

        boolean isDelta();

        String getDeltaFilename();

        String getDeltaPath();

        String getDeltaMd5();
    }
	
	public interface Listener {

        void onCheck();

        void onCheckError(String cause);
		
		void onUpdateFound(UpdateInfo[] info);
    }
	
	public class Client {
	    private static final String TAG = "Cota: Client";
		private static final String URL = "http://get.cypheros.co/updates/getter.php?d=%s";
		
		private String mDevice = null;
		private String mError = null;
		private VersionManager mVersion;
		
		public String getUrl(String device, VersionManager version) {
			mDevice = device;
			mVersion = version;
			return String.format(URL, device);
		}
		
		public List<UpdateInfo> createPackageInfoList(JSONObject response) throws Exception {
			mError = null;
			List<UpdateInfo> list = new ArrayList<>();
			mError = response.optString("error");
			if (mError == null || mError.isEmpty()) {
				JSONArray updates = response.getJSONArray("updates");
				for (int i = updates.length() - 1; i >= 0; i--) {
					JSONObject file = updates.getJSONObject(i);
					String filename = file.getString("name");
					String versionString  = file.getString("version");
					String dateString     = file.getString("build");

					if (Constants.DEBUG) Log.d(TAG, "version from server: " + versionString + " " + dateString);
					VersionManager version = new VersionManager(versionString, dateString);
					if (VersionManager.compare(mVersion, version) < 0) {
						list.add(new UpdatePackage(mDevice, filename, version, file.getString("size"),
						        file.getString("url"), file.getString("md5"), file.getString("text")));
						if (Constants.DEBUG) Log.d(TAG, "A new version is found!");
					}
				}
			}
			Collections.sort(list, new Comparator<Updater.PackageInfo>() {
			    @Override
				public int compare(Updater.PackageInfo lhs, Updater.PackageInfo rhs) {
					return VersionManager.compare(lhs.getVersion(), rhs.getVersion());
				}
			});
			Collections.reverse(list);
			return list;
		}
		
		public String getError() {
			return mError;
		}
	}
}