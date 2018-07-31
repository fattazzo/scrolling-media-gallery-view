/*
 * Project: scrolling-media-gallery-view
 * File: VideoLoader.kt
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

package com.gmail.fattazzo.scrollingmediagalleryview.loaders.video

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.AsyncTask
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.gmail.fattazzo.scrollingmediagalleryview.R
import com.gmail.fattazzo.scrollingmediagalleryview.actions.MediaAction
import com.gmail.fattazzo.scrollingmediagalleryview.loaders.MediaLoader
import com.gmail.fattazzo.scrollingmediagalleryview.loaders.MediaType


/**
 * @author fattazzo
 *         <p/>
 *         date: 27/06/18
 */
class VideoLoader(private val videoUrl: String) : MediaLoader() {

    private var imageBitmap: Bitmap? = null
    private var thumbBitmap: Bitmap? = null
    private var previewLoaded: Boolean = false

    override fun getType(): MediaType = MediaType.VIDEO

    override fun loadMedia(context: Context, index: String, loadingProgressBar: ProgressBar, mediaImageView: ImageView, actionView: ImageView) {
        LoadVideoImageTask(context, index, loadingProgressBar, mediaImageView, actionView, addVideoAction = true, url = videoUrl).execute()
    }

    override fun loadThumbnail(context: Context, index: String, loadingProgressBar: ProgressBar, mediaImageView: ImageView, overlayImageView: ImageView) {
        LoadVideoImageTask(context, index, loadingProgressBar, mediaImageView, overlayImageView, asThumb = true, url = videoUrl).execute()
    }

    override fun executeAction(context: Context, mediaAction: MediaAction) {
        mediaAction.execute(context, videoUrl, MediaType.VIDEO)
    }

    private inner class LoadVideoImageTask(val context: Context, val index: String, val progressBar: ProgressBar?,
                                           val imageView: ImageView?, val actionView: ImageView,
                                           val addVideoAction: Boolean = false, val asThumb: Boolean = false,
                                           val url: String) : AsyncTask<Void, Void, Bitmap?>() {

        override fun onPreExecute() {
            progressBar?.visibility = View.VISIBLE
            imageView?.setImageBitmap(null)
        }

        override fun doInBackground(vararg params: Void?): Bitmap? {
            if (!previewLoaded) {
                val mediaMetadataRetriever = MediaMetadataRetriever()
                try {
                    mediaMetadataRetriever.setDataSource(url, HashMap())
                    imageBitmap = mediaMetadataRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST)
                    //thumbBitmap = Bitmap.createScaledBitmap(imageBitmap, thumbWidth, thumbHeight, false)
                    thumbBitmap = imageBitmap
                } catch (e: Exception) {
                    imageBitmap = null
                    thumbBitmap = null
                } finally {
                    previewLoaded = true
                    mediaMetadataRetriever.release()
                }
            }
            return if (asThumb) thumbBitmap else imageBitmap
        }

        override fun onPostExecute(result: Bitmap?) {
            if (imageView?.tag.toString() === index) {
                imageView?.setImageBitmap(result)

                actionView.visibility = View.VISIBLE
                actionView.setImageResource(R.drawable.smgv_placeholder_video)

                if (addVideoAction) {
                    actionView.setOnClickListener {
                        val i = Intent(Intent.ACTION_VIEW)
                        i.setDataAndType(Uri.parse(url), "video/*")
                        context.startActivity(i)
                    }
                }
            }

            progressBar?.visibility = View.GONE
        }
    }
}