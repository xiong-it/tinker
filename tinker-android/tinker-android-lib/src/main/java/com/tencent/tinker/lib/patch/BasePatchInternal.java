/*
 * Copyright (C) 2016 Tencent WeChat, Inc.
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

package com.tencent.tinker.lib.patch;

import com.tencent.tinker.lib.util.TinkerLog;
import com.tencent.tinker.loader.shareutil.ShareConstants;
import com.tencent.tinker.loader.shareutil.SharePatchFileUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by zhangshaowen on 16/4/12.
 */
public class BasePatchInternal {
    protected static final String TAG = "BasePatchInternal";

    protected static final String DEX_PATH             = ShareConstants.DEX_PATH;
    protected static final String SO_PATH              = ShareConstants.SO_PATH;
    protected static final String DEX_OPTIMIZE_PATH    = ShareConstants.DEX_OPTIMIZE_PATH;
    protected static final int    MAX_EXTRACT_ATTEMPTS = ShareConstants.MAX_EXTRACT_ATTEMPTS;
    protected static final String DEX_META_FILE        = ShareConstants.DEX_META_FILE;
    protected static final String SO_META_FILE         = ShareConstants.SO_META_FILE;
//    /**
//     *  TODO: not need to generate temp files
//     */
//    private static final String PATCH_SUFFIX         = "_patch";
//    private static final String APK_SUFFIX           = "_apk";

    protected static final int TYPE_DEX     = ShareConstants.TYPE_DEX;
    protected static final int TYPE_Library = ShareConstants.TYPE_LIBRARY;

    public static boolean extract(ZipFile zipFile, ZipEntry entryFile, File extractTo, String targetMd5, boolean isDex) throws IOException {
        int numAttempts = 0;
        boolean isExtractionSuccessful = false;
        while (numAttempts < MAX_EXTRACT_ATTEMPTS && !isExtractionSuccessful) {
            numAttempts++;
            BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entryFile));
            FileOutputStream fos = new FileOutputStream(extractTo);
            BufferedOutputStream out = new BufferedOutputStream(fos);

            TinkerLog.i(TAG, "try Extracting " + extractTo.getPath());

            try {
                byte[] buffer = new byte[ShareConstants.BUFFER_SIZE];
                int length = bis.read(buffer);
                while (length != -1) {
                    out.write(buffer, 0, length);
                    length = bis.read(buffer);
                }
            } finally {
                SharePatchFileUtil.closeQuietly(out);
                SharePatchFileUtil.closeQuietly(bis);
            }

            if (isDex) {
                isExtractionSuccessful = SharePatchFileUtil.verifyDexFileMd5(extractTo, targetMd5);
            } else {
                isExtractionSuccessful = SharePatchFileUtil.verifyFileMd5(extractTo, targetMd5);
            }
            TinkerLog.i(TAG, "isExtractionSuccessful: %b", isExtractionSuccessful);

            if (!isExtractionSuccessful) {
                extractTo.delete();
                if (extractTo.exists()) {
                    TinkerLog.e(TAG, "Failed to delete corrupted dex " + extractTo.getPath());
                }
            }
        }

        return isExtractionSuccessful;
    }

    public static int getMetaCorruptedCode(int type) {
        if (type == TYPE_DEX) {
            return ShareConstants.ERROR_PACKAGE_CHECK_DEX_META_CORRUPTED;
        } else if (type == TYPE_Library) {
            return ShareConstants.ERROR_PACKAGE_CHECK_LIB_META_CORRUPTED;
        }
        return ShareConstants.ERROR_PACKAGE_CHECK_OK;
    }
}
