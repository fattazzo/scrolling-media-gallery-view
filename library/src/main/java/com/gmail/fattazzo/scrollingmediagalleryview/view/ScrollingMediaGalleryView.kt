/*
 * Project: scrolling-media-gallery-view
 * File: ScrollingMediaGalleryView.kt
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

package com.gmail.fattazzo.scrollingmediagalleryview.view

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.support.constraint.ConstraintLayout
import android.support.constraint.Guideline
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PagerSnapHelper
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.gmail.fattazzo.scrollingmediagalleryview.MediaManager
import com.gmail.fattazzo.scrollingmediagalleryview.R
import com.gmail.fattazzo.scrollingmediagalleryview.actions.MediaAction
import com.gmail.fattazzo.scrollingmediagalleryview.adapters.MediaAdapter
import com.gmail.fattazzo.scrollingmediagalleryview.adapters.ThumbAdapter
import com.gmail.fattazzo.scrollingmediagalleryview.loaders.MediaLoader


/**
 * @author fattazzo
 *         <p/>
 *         date: 28/06/18
 */
class ScrollingMediaGalleryView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var thumbRecyclerView: RecyclerView
    private val thumbsLayoutManager = LinearLayoutManager(context, if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        LinearLayout.HORIZONTAL
    } else {
        LinearLayout.VERTICAL
    }, false)
    private var thumbsAdapter: ThumbAdapter

    private var recyclerView: RecyclerView
    private val layoutManager = LinearLayoutManager(context, LinearLayout.HORIZONTAL, false)
    private var mediaAdapter: MediaAdapter

    private var medialList: MutableList<MediaLoader>

    private var toggleThumbsButton: ImageButton
    private var guidelineThumbContentToggle: Guideline
    private var guidelineThumbContent: Guideline

    private val mediaActions: MutableMap<Class<out MediaLoader>, MutableList<MediaAction>> = mutableMapOf()

    private val fabMenu: FloatingActionMenu

    var showThumbs = true
        set(value) {
            field = value
            updateThumbsView()
        }
    var thumbsHeightPercentage = 0.9f
        set(value) {
            field = value
            updateThumbsView()
        }

    init {
        View.inflate(context, R.layout.smgv_scrolling_media_gallery_view, this)

        fabMenu = findViewById(R.id.smgv_fab_menu)
        fabMenu.tag = -1

        medialList = mutableListOf()

        thumbRecyclerView = findViewById(R.id.smgv_thumbRecyclerView)
        thumbRecyclerView.layoutManager = thumbsLayoutManager
        thumbsAdapter = ThumbAdapter(context, medialList)
        thumbRecyclerView.adapter = thumbsAdapter

        recyclerView = findViewById(R.id.smgvRecyclerView)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
        recyclerView.layoutManager = layoutManager
        mediaAdapter = MediaAdapter(context, medialList)
        recyclerView.adapter = mediaAdapter
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position = layoutManager.findFirstVisibleItemPosition()
                    MediaManager.fireCurrentPositionChanged(position, MediaManager.SourcePosition.PREVIEW)
                }
            }
        })

        guidelineThumbContentToggle = findViewById(R.id.smgv_guidelineThumbContentToggle)
        guidelineThumbContent = findViewById(R.id.smgv_guidelineThumbContent)

        toggleThumbsButton = findViewById(R.id.smgv_toggle_thumbs_button)
        toggleThumbsButton.setOnClickListener { toggleFullScreen() }

        initCustomAttrs(attrs)

        MediaManager.removeListeners()
        MediaManager.addPositionListener(object : MediaManager.PositionListener(MediaManager.SourcePosition.PREVIEW) {
            override fun onChange(oldPosition: Int, newPosition: Int) {
                layoutManager.scrollToPosition(newPosition)
            }
        })
        MediaManager.addPositionListener(object : MediaManager.PositionListener(MediaManager.SourcePosition.THUMBNAIL) {
            override fun onChange(oldPosition: Int, newPosition: Int) {
                thumbsLayoutManager.scrollToPosition(newPosition)
            }
        })
        MediaManager.addPositionListener(object : MediaManager.PositionListener(MediaManager.SourcePosition.NONE) {
            override fun onChange(oldPosition: Int, newPosition: Int) {
                updateMediaActionView()
            }
        })
    }

    private fun initCustomAttrs(attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ScrollingMediaGalleryView, 0, 0)
        try {
            showThumbs = ta.getBoolean(R.styleable.ScrollingMediaGalleryView_smgv_show_thumbs, true)
            thumbsHeightPercentage = ta.getFloat(R.styleable.ScrollingMediaGalleryView_smgv_thumbs_height_percentage, 0.9f)
            if (thumbsHeightPercentage > 1 || thumbsHeightPercentage < 0) thumbsHeightPercentage = 0.9f
        } finally {
            ta.recycle()
        }
    }

    fun addMedia(media: MediaLoader) {
        addMedia(listOf(media))
    }

    fun addMedia(media: List<MediaLoader>) {
        medialList.addAll(media)
        Handler(Looper.getMainLooper()).post {
            thumbRecyclerView.adapter.notifyDataSetChanged()
            recyclerView.adapter.notifyDataSetChanged()
            updateMediaActionView()
        }
    }

    fun clear() {
        medialList.clear()
        Handler(Looper.getMainLooper()).post {
            thumbRecyclerView.adapter.notifyDataSetChanged()
            recyclerView.adapter.notifyDataSetChanged()
            updateMediaActionView()
        }
    }

    private fun toggleFullScreen() {
        val guidelinePercentage = (guidelineThumbContentToggle.layoutParams as ConstraintLayout.LayoutParams).guidePercent

        val endPos = if (guidelinePercentage == thumbsHeightPercentage) 1f else thumbsHeightPercentage
        val guidelineAnimator = ValueAnimator.ofFloat(guidelinePercentage, endPos)
        guidelineAnimator.addUpdateListener { animation ->
            guidelineThumbContentToggle.setGuidelinePercent(animation.animatedValue as Float)
            guidelineThumbContentToggle.requestLayout()
        }
        guidelineAnimator.duration = 300L

        val start = fabMenu.alpha
        val end = if (start == 1f) 0f else 1f
        val actionMenuAnimator = ValueAnimator.ofFloat(start, end)
        actionMenuAnimator.addUpdateListener { animation ->
            fabMenu.alpha = animation.animatedValue as Float
            fabMenu.requestLayout()
        }
        actionMenuAnimator.duration = 300L

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(guidelineAnimator, actionMenuAnimator)
        animatorSet.start()
    }

    private fun updateMediaActionView() {

        val actions = try {
            val mediaLoader = medialList[MediaManager.currentPosition]
            mediaActions[mediaLoader.javaClass]
        } catch (e: Exception) {
            null
        }

        fabMenu.close(true)
        fabMenu.removeAllMenuButtons()
        fabMenu.tag = MediaManager.currentPosition

        if (actions.orEmpty().isEmpty()) {
            fabMenu.visibility = View.GONE
        } else {
            fabMenu.visibility = View.VISIBLE

            actions!!.forEach {
                val button = FloatingActionButton(context)
                button.labelText = resources.getString(it.getLabelResId())
                button.setImageResource(it.getIconResId())
                button.buttonSize = FloatingActionButton.SIZE_MINI
                val action = it
                button.setOnClickListener {
                    try {
                        val positon = layoutManager.findFirstVisibleItemPosition()
                        medialList[positon].executeAction(context, action)
                        fabMenu.close(true)
                    } catch (e: Exception) {
                        println("Errore: " + e.message)
                    }
                }
                fabMenu.addMenuButton(button)
            }
        }
    }

    fun addMediaAction(mediaLoaderClass: Class<out MediaLoader>, mediaAction: MediaAction) {
        if (!mediaActions.containsKey(mediaLoaderClass)) {
            mediaActions[mediaLoaderClass] = mutableListOf(mediaAction)
        } else {
            mediaActions[mediaLoaderClass]!!.add(mediaAction)
        }
    }

    private fun updateThumbsView() {
        guidelineThumbContentToggle.setGuidelinePercent(if (showThumbs) thumbsHeightPercentage else 1f)
        guidelineThumbContent.setGuidelinePercent(if (showThumbs) thumbsHeightPercentage else 1f)
    }
}