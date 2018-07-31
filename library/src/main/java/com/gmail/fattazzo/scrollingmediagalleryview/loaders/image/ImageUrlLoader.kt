/*
 * Project: scrolling-media-gallery-view
 * File: ImageUrlLoader.kt
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

package com.gmail.fattazzo.scrollingmediagalleryview.loaders.image

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.gmail.fattazzo.scrollingmediagalleryview.R
import com.gmail.fattazzo.scrollingmediagalleryview.actions.MediaAction
import com.gmail.fattazzo.scrollingmediagalleryview.loaders.MediaLoader
import com.gmail.fattazzo.scrollingmediagalleryview.loaders.MediaType
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

/**
 * @author fattazzo
 *         <p/>
 *         date: 27/06/18
 */
class ImageUrlLoader(private val imageUrl: String) : MediaLoader() {

    override fun getType() = MediaType.IMAGE

    override fun loadMedia(context: Context, index: String, loadingProgressBar: ProgressBar, mediaImageView: ImageView, actionView: ImageView) {
        loadImage(loadingProgressBar, mediaImageView, actionView)
    }

    override fun loadThumbnail(context: Context, index: String, loadingProgressBar: ProgressBar, mediaImageView: ImageView, overlayImageView: ImageView) {
        loadImage(loadingProgressBar, mediaImageView, overlayImageView, true)
    }

    private fun loadImage(loadingProgressBar: ProgressBar, mediaImageView: ImageView, overlayImageView: ImageView, asThumb: Boolean = false) {
        loadingProgressBar.visibility = View.VISIBLE
        overlayImageView.visibility = View.GONE
        var rc = Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.smgv_placeholder_image)
                .error(R.drawable.smgv_media_loading_error)

        if (asThumb) {
            //rc = rc.resize(thumbWidth, thumbHeight).centerInside()
        }

        rc.into(mediaImageView as ImageView?, object : Callback {
            override fun onSuccess() {
                loadingProgressBar.visibility = View.GONE
            }

            override fun onError(e: Exception?) {
                loadingProgressBar.visibility = View.GONE
            }
        })
    }

    override fun executeAction(context: Context, mediaAction: MediaAction) {
        mediaAction.execute(context, imageUrl, MediaType.IMAGE)
    }
}