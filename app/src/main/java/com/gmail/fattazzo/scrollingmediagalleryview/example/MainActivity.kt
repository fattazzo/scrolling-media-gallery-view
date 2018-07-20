/*
 * Project: scrolling-media-gallery-view
 * File: MainActivity.kt
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

package com.gmail.fattazzo.scrollingmediagalleryview.example

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.gmail.fattazzo.scrollingmediagalleryview.actions.MediaActionDownload
import com.gmail.fattazzo.scrollingmediagalleryview.actions.MediaActionShare
import com.gmail.fattazzo.scrollingmediagalleryview.loaders.image.ImageUrlLoader
import com.gmail.fattazzo.scrollingmediagalleryview.loaders.video.VideoLoader
import com.gmail.fattazzo.scrollingmediagalleryview.utils.PermissionsUtil
import com.gmail.fattazzo.scrollingmediagalleryview.view.ScrollingMediaGalleryView
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.IOException

class MainActivity : AppCompatActivity() {

    internal lateinit var scrollGalleryView: ScrollingMediaGalleryView

    private var imagesUrl = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        PermissionsUtil.askPermissions(this)

        scrollGalleryView = findViewById(R.id.scroll_gallery_view)

        addMediaActions()

        if (savedInstanceState != null && savedInstanceState.containsKey("imagesUrl")) {
            imagesUrl = savedInstanceState.getStringArrayList("imagesUrl")
            scrollGalleryView.clear()
            scrollGalleryView.addMedia(VideoLoader("https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4"))
            scrollGalleryView.addMedia(VideoLoader("https://italiancoders.it/wp-content/uploads/2018/06/android-constraintlayout-carousel.mp4"))
            scrollGalleryView.addMedia(imagesUrl.map { ImageUrlLoader(it) })
        } else {
            scrollGalleryView.addMedia(VideoLoader("https://archive.org/download/ksnn_compilation_master_the_internet/ksnn_compilation_master_the_internet_512kb.mp4"))
            scrollGalleryView.addMedia(VideoLoader("https://italiancoders.it/wp-content/uploads/2018/06/android-constraintlayout-carousel.mp4"))
            val fetcher = ImagesFetcher()
            fetcher.execute()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putSerializable("imagesUrl", imagesUrl)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionsUtil.PERMISSION_ALL -> {
                PermissionsUtil.processPermission(this, permissions, grantResults)
            }
        }
    }

    private fun addMediaActions() {
        scrollGalleryView.addMediaAction(VideoLoader::class.java, MediaActionDownload())
        scrollGalleryView.addMediaAction(VideoLoader::class.java, MediaActionShare())

        scrollGalleryView.addMediaAction(ImageUrlLoader::class.java, MediaActionDownload())
        scrollGalleryView.addMediaAction(ImageUrlLoader::class.java, MediaActionShare())
    }

    private inner class ImagesFetcher : AsyncTask<Void, String, List<String>>() {

        val imageElements: Elements
            @Throws(IOException::class)
            get() = Jsoup.connect("https://www.freeimages.com/")
                    .get()
                    .getElementById("content")
                    .getElementsByTag("img")

        override fun doInBackground(vararg params: Void): List<String> {
            try {
                val images = ArrayList<String>()
                var counter = 0
                for (imageElm in imageElements) {
                    val image = imageElm.attr("src")
                    publishProgress(image)
                    images.add(image)
                    if (++counter == 20) break
                }

                return images
            } catch (e: IOException) {
                Log.e(javaClass.name, "Cannot load image urls", e)
            }

            return emptyList()
        }

        override fun onPreExecute() {
            imagesUrl.clear()
        }

        override fun onPostExecute(imageUrls: List<String>) {
        }

        override fun onProgressUpdate(vararg value: String) {
            value.let {
                imagesUrl.add(it[0])
                scrollGalleryView.addMedia(ImageUrlLoader(it[0]))
            }
        }
    }
}
