/*
 * Project: scrolling-media-gallery-view
 * File: MediaAdapter.kt
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

package com.gmail.fattazzo.scrollingmediagalleryview.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gmail.fattazzo.scrollingmediagalleryview.R
import com.gmail.fattazzo.scrollingmediagalleryview.loaders.MediaLoader
import kotlinx.android.synthetic.main.smgv_media_loader_view.view.*

/**
 * @author fattazzo
 *         <p/>
 *         date: 11/07/18
 */
class MediaAdapter(private val context: Context, private var loaders: MutableList<MediaLoader>) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    override fun getItemCount(): Int = loaders.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.smgv_media_loader_view, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val loader = loaders[position]

        val second = position * 3 - 3
        val tag = second.toString()

        holder.imageView.tag = tag
        loader.loadMedia(context, tag, holder.progressBar, holder.imageView, holder.actionView)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView = itemView.smgv_media_loader_imageview!!
        var actionView = itemView.smgv_media_loader_action_imageView!!
        var progressBar = itemView.smgv_media_loader_progressBar!!
    }
}