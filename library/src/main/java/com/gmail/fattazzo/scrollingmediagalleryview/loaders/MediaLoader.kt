/*
 * Project: scrolling-media-gallery-view
 * File: MediaLoader.kt
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

package com.gmail.fattazzo.scrollingmediagalleryview.loaders

import android.content.Context
import android.widget.ImageView
import android.widget.ProgressBar
import com.gmail.fattazzo.scrollingmediagalleryview.actions.MediaAction

/**
 * @author fattazzo
 *         <p/>
 *         date: 27/06/18
 */
abstract class MediaLoader(val thumbWidth: Int = 100, val thumbHeight: Int = 100) {

    /**
     * Type of media. @see [MediaType]
     */
    abstract fun getType(): MediaType

    /**
     * Load image and set it to mediaLoaderView.
     */
    abstract fun loadMedia(context: Context, index: String, loadingProgressBar: ProgressBar, mediaImageView: ImageView, actionView: ImageView)

    /**
     * Load thumbnail of the image and set it to imageView.
     */
    abstract fun loadThumbnail(context: Context, index: String, loadingProgressBar: ProgressBar, mediaImageView: ImageView, overlayImageView: ImageView)

    /**
     * Execute the media action
     */
    abstract fun executeAction(context: Context, mediaAction: MediaAction)
}