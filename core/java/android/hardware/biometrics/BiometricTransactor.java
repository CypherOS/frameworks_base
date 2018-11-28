/*
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

package android.hardware.biometrics;

import android.content.Context;
import android.content.res.Resources;
import android.os.HwBinder;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.R;


/**
 * Class that applies hardware transactions to the
 * biometrics hal.
 * @hide
 */
public class BiometricTransactor {

    int BIOMETRIC_NAVIGATION_ENABLE = 41;

    int BIOMETRIC_NAVIGATION_DISABLE = 42;

    private static final String TAG = "BiometricTransactor";

    private static String mDescriptor;
    private static int mTransactionId;

    private static IHwBinder sBiometricTransactor;

    public BiometricTransactor(Context context) throws RemoteException {
        mDescriptor = context.getResources().getString(
                R.string.config_vendorBiometricsDescriptor);
        mTransactionId = context.getResources().getInteger(
                R.integer.config_vendorBiometricsTransactionId);
        sBiometricTransactor = HwBinder.getService(mDescriptor, "default");
    }

    public int sendCmdToHal(int cmdId) {
        if (sBiometricTransactor == null) {
            return -1;
        }

        if (TextUtils.isEmpty(mDescriptor)) {
            Log.d(TAG, "Descriptor returned null");
            return -1;
        }

        if (mTransactionId == 0) {
            Log.d(TAG, "Transaction ID returned null");
            return -1;
        }

        HwParcel data = new HwParcel();
        HwParcel reply = new HwParcel();

        try {
            data.writeInterfaceToken(mDescriptor);
            data.writeInt32(cmdId);

            sBiometricTransactor.transact(mTransactionId, data, reply, 0);

            reply.verifySuccess();
            data.releaseTemporaryStorage();
            return reply.readInt32();
        } catch (Throwable t) {
            return -1;
        } finally {
            reply.release();
        }
    }

    public int sendMsgEnable() {
        return BIOMETRIC_NAVIGATION_ENABLE;
    }

    public int sendMsgDisable() {
        return BIOMETRIC_NAVIGATION_DISABLE;
    }
}
