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
package android.client.utils;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Scanner;

public class FileUtils {

    public static final String DOWNLOAD_PATH = new File(Environment
            .getExternalStorageDirectory(), "Updates/").getAbsolutePath();
    private static final String SDCARD = Environment.getExternalStorageDirectory()
            .getAbsolutePath();
    private static final String PREFIX = "aoscp_";
    private static final String SUFFIX = ".zip";

    private static String sPrimarySdcard;
    private static String sSecondarySdcard;
    private static boolean sSdcardsChecked;

    public static void init(Context context) {
        File downloads = new File(DOWNLOAD_PATH);

        downloads.mkdirs();

        readMounts(context);
    }

    private static String[] getDownloadList(Context context) {
        File downloads = initSettingsHelper(context);
        ArrayList<String> list = new ArrayList<>();
        try {
            for (File f : downloads.listFiles()) {
                if (isRom(f.getName())) {
                    list.add(f.getName());
                }
            }
        } catch (NullPointerException e) {
            // blah
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[] getDownloadSizes(Context context) {
        File downloads = initSettingsHelper(context);
        ArrayList<String> list = new ArrayList<>();
        for (File f : downloads.listFiles()) {
            if (isRom(f.getName())) {
                list.add(humanReadableByteCount(f.length(), false));
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static String getDownloadSize(Context context, String fileName) {
        File downloads = initSettingsHelper(context);
        for (String file : getDownloadList(context)) {
            if (fileName.equals(file)) {
                File f = new File(downloads, fileName);
                return humanReadableByteCount(f.length(), false);
            }
        }
        return "0";
    }
	
	public static File getFile(Context context, String fileName) {
        File downloads = initSettingsHelper(context);
        for (File f : downloads.listFiles()) {
            if (f.getName().equals(fileName)) {
                return f;
            }
        }
        return null; //The downloaded file couldn't be located
    }

    public static boolean isOnDownloadList(Context context, String fileName) {
        for (String file : getDownloadList(context)) {
            if (fileName.equals(file))
                return true;
        }
        return false;
    }

    private static boolean isRom(String name) {
        return name.startsWith(PREFIX) && name.endsWith(SUFFIX);
    }

    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static boolean isInSecondaryStorage(String path) {
        return !path.startsWith(sPrimarySdcard) && !path.startsWith("/sdcard")
                && !path.startsWith("/mnt/sdcard");
    }

    public static boolean hasSecondarySdCard() {
        return sSecondarySdcard != null;
    }

    public static String getPrimarySdCard() {
        return sPrimarySdcard;
    }

    public static String getSecondarySdCard() {
        return sSecondarySdcard;
    }

    private static void readMounts(Context context) {
        if (sSdcardsChecked) {
            return;
        }

        ArrayList<String> mounts = new ArrayList<>();
        ArrayList<String> vold = new ArrayList<>();

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File("/proc/mounts"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith("/dev/block/vold/")) {
                    String[] lineElements = line.split(" ");
                    String element = lineElements[1];

                    mounts.add(element);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        boolean addExternal = mounts.size() == 1 && isExternalStorageAvailable();
        if (mounts.size() == 0 && addExternal) {
            mounts.add("/mnt/sdcard");
        }
        File fstab = findFstab();
        scanner = null;
        if (fstab != null) {
            try {

                scanner = new Scanner(fstab);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("dev_mount")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[2];

                        if (element.contains(":")) {
                            element = element.substring(0, element.indexOf(":"));
                        }

                        if (!element.toLowerCase().contains("usb")) {
                            vold.add(element);
                        }
                    } else if (line.startsWith("/devices/platform")) {
                        String[] lineElements = line.split(" ");
                        String element = lineElements[1];

                        if (element.contains(":")) {
                            element = element.substring(0, element.indexOf(":"));
                        }

                        if (!element.toLowerCase().contains("usb")) {
                            vold.add(element);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
            }
        }
        if (addExternal && (vold.size() == 1 && isExternalStorageAvailable())) {
            mounts.add(vold.get(0));
        }
        if (vold.size() == 0 && isExternalStorageAvailable()) {
            vold.add("/mnt/sdcard");
        }

        for (int i = 0; i < mounts.size(); i++) {
            String mount = mounts.get(i);
            File root = new File(mount);
            if (!vold.contains(mount)
                    || (!root.exists() || !root.isDirectory() || !root.canWrite())) {
                mounts.remove(i--);
            }
        }

        for (int i = 0; i < mounts.size(); i++) {
            String mount = mounts.get(i);
            if (!mount.contains("sdcard0") && !mount.equalsIgnoreCase("/mnt/sdcard")
                    && !mount.equalsIgnoreCase("/sdcard")) {
                sSecondarySdcard = mount;
            } else {
                sPrimarySdcard = mount;
            }
        }

        if (sPrimarySdcard == null) {
            sPrimarySdcard = "/sdcard";
        }

        sSdcardsChecked = true;
    }

    private static File findFstab() {
        File file = null;

        file = new File("/system/etc/vold.fstab");
        if (file.exists()) {
            return file;
        }

        String fstab = exec("grep -ls \"/dev/block/\" * --include=fstab.* --exclude=fstab.goldfish");
        if (fstab != null) {
            String[] files = fstab.split("\n");
            for (String file1 : files) {
                file = new File(file1);
                if (file.exists()) {
                    return file;
                }
            }
        }

        return null;
    }
	
	public static String exec(String command) {
        try {
            Process p = Runtime.getRuntime().exec(command);
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("sync\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();
            return getStreamLines(p.getInputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
	
	private static String getStreamLines(final InputStream is) {
        String out = null;
        StringBuffer buffer = null;
        final DataInputStream dis = new DataInputStream(is);

        try {
            if (dis.available() > 0) {
                buffer = new StringBuffer(dis.readLine());
                while (dis.available() > 0) {
                    buffer.append("\n").append(dis.readLine());
                }
            }
            dis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (buffer != null) {
            out = buffer.toString();
        }
        return out;
    }

    public static double getSpaceLeft() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getAvailableBlocksLong()
                * (double) stat.getBlockSizeLong();
        // One binary gigabyte equals 1,073,741,824 bytes.
        return sdAvailSize / 1073741824;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMG" : "KMG").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre).replace(",", ".");
    }

    public static String md5(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String md5 = bigInt.toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
            return md5;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                is.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static File initSettingsHelper(Context context) {
        File downloads = new File(DOWNLOAD_PATH);
        downloads.mkdirs();
        return downloads;
    }

    public static boolean hasAndroidSecure() {
        return folderExists(SDCARD + "/.android-secure");
    }

    public static boolean hasSdExt() {
        return folderExists("/sd-ext");
    }

    private static boolean folderExists(String path) {
        File f = new File(path);
        return f.exists() && f.isDirectory();
    }
}