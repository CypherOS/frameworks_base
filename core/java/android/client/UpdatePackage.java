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
import android.client.utils.FileUtils;

import java.io.Serializable;

public class UpdatePackage implements UpdateManager.UpdateInfo, Serializable {

    private String mMd5 = null;
    private String mIncrementalMd5 = null;
    private String mFilename = null;
    private String mIncrementalFilename = null;
    private String mPath = null;
    private String mHost = null;
    private String mSize = null;
	private String mText = null;
    private String mIncrementalPath = null;
    private VersionManager mVersion;
    private boolean mIsDelta = false;

    public UpdatePackage(String device, String name, VersionManager version, long size, String url,
                         String md5, String text) {
        this(device, name, version,
                FileUtils.humanReadableByteCount(size, false), url, md5, text);
    }

    public UpdatePackage(String device, String name, VersionManager version, String size, String url,
                         String md5, String text) {
        mFilename = name;
        mVersion = version;
        mSize = size;
        mPath = url;
        mMd5 = md5;
		mText = text;
        mHost = mPath.replace("http://", "");
        mHost = mHost.replace("https://", "");
        mHost = mHost.substring(0, mHost.indexOf("/"));
    }

    @Override
    public boolean isDelta() {
        return mIsDelta;
    }

    @Override
    public String getDeltaFilename() {
        return mIncrementalFilename;
    }

    @Override
    public String getDeltaPath() {
        return mIncrementalPath;
    }

    @Override
    public String getDeltaMd5() {
        return mIncrementalMd5;
    }

    @Override
    public String getMd5() {
        return mMd5;
    }

    @Override
    public String getFilename() {
        return mFilename;
    }

    @Override
    public String getPath() {
        return mPath;
    }

    @Override
    public String getHost() {
        return mHost;
    }

    @Override
    public VersionManager getVersion() {
        return mVersion;
    }

    @Override
    public String getSize() {
        return mSize;
    }
	
	@Override
    public String getText() {
        return mText;
    }
}