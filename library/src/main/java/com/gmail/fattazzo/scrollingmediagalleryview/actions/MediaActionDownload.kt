/*
 * Project: scrolling-media-gallery-view
 * File: MediaActionDownload.kt
 *
 * Created by fattazzo
 * Copyright © 2018 Gianluca Fattarsi. All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gmail.fattazzo.scrollingmediagalleryview.actions

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.gmail.fattazzo.scrollingmediagalleryview.R
import com.gmail.fattazzo.scrollingmediagalleryview.loaders.MediaType
import com.gmail.fattazzo.scrollingmediagalleryview.utils.PermissionsUtil
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author fattazzo
 *         <p/>
 *         date: 17/07/18
 */
class MediaActionDownload : MediaAction {

    override fun getIconResId(): Int = R.drawable.smgv_file_download

    override fun getLabelResId(): Int = R.string.smgv_media_action_download

    override fun execute(context: Context, path: String, mediaType: MediaType) {
        val uri = Uri.parse(path)

        // Check if permission is granted
        if (!PermissionsUtil.checkPermissions(context, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
            Toast.makeText(context, R.string.smgv_permission_not_granted, Toast.LENGTH_LONG).show()
            return
        }

        // Build the request with title, message and file of download
        val request = DownloadManager.Request(uri)
        request.setTitle(getDownloadTitle(context))
        request.setDescription(getDownloadMessage(context))
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getMediaFileName(context, mediaType, uri))

        // Launch download via Download Service
        val downloadManager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    /**
     * Retrieve the download title.
     * Use main application resource R.string.app_name if exist, "Media Download" else
     */
    private fun getDownloadTitle(context: Context): String {
        val checkExistence = context.resources.getIdentifier("app_name", "string", context.packageName)
        return if (checkExistence != 0) {
            "Media Download"
        } else {
            context.getString(checkExistence)
        }
    }

    /**
     * Media download description based on R.string.smgv_media_download_message resource
     */
    private fun getDownloadMessage(context: Context): String = context.getString(R.string.smgv_media_download_message)

    /**
     * Build media file name.
     */
    private fun getMediaFileName(context: Context, mediaType: MediaType, uri: Uri): String {
        val mediaTypeName = mediaType.name.toLowerCase().capitalize()
        val dateString = SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault()).format(Calendar.getInstance().time)
        val extension = getMimeType(context, uri)
        return "${mediaTypeName}_$dateString.$extension"
    }
}