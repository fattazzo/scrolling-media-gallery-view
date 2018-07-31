/*
 * Project: scrolling-media-gallery-view
 * File: MediaActionShare.kt
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

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import com.gmail.fattazzo.scrollingmediagalleryview.R
import com.gmail.fattazzo.scrollingmediagalleryview.loaders.MediaType
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URL


/**
 * @author fattazzo
 *         <p/>
 *         date: 17/07/18
 */
class MediaActionShare : MediaAction {

    override fun getIconResId(): Int = R.drawable.smgv_share

    override fun getLabelResId(): Int = R.string.smgv_media_action_share

    override fun execute(context: Context, path: String, mediaType: MediaType) {
        when (mediaType) {
            MediaType.IMAGE -> shareImage(context, path)
            MediaType.VIDEO -> shareVideo(context, path)
            else -> return
        }
    }

    private fun shareImage(context: Context, imagePath: String) {
        Picasso.get().load(imagePath).into(object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            }

            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                try {
                    //saving image
                    val extension = getMimeType(context, Uri.parse(imagePath)) ?: "jpg"
                    File(context.externalCacheDir, "smgvimages").deleteRecursively() // delete old images
                    val cachePath = File(context.externalCacheDir, "smgvimages")
                    cachePath.mkdirs() // don't forget to make the directory
                    val file = File(context.externalCacheDir, "smgvimages/image.$extension")
                    val fOut = FileOutputStream(file)
                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                    fOut.flush()
                    fOut.close()
                    file.setReadable(true, false)


                    // SHARE
                    //val imagePath = File(context.externalCacheDir, "smgvimages")
                    val shareIntent = Intent()
                    shareIntent.action = Intent.ACTION_SEND
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
                    shareIntent.type = "image/*"
                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
                    context.startActivity(Intent.createChooser(shareIntent, "Choose app"))
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        })
    }

    private fun shareVideo(context: Context, videoPath: String) {

        val asyncTask = object : AsyncTask<String, Void, File>() {
            override fun doInBackground(vararg params: String?): File {
                //saving video
                val extension = getMimeType(context, Uri.parse(params[0])) ?: "mp4"
                File(context.externalCacheDir, "smgvvideo").deleteRecursively() // delete old images
                val cachePath = File(context.externalCacheDir, "smgvvideo")
                cachePath.mkdirs() // don't forget to make the directory
                val file = File(context.externalCacheDir, "smgvvideo/video.$extension")

                val u = URL(videoPath)
                val conn = u.openConnection()
                val contentLength = conn.contentLength

                val stream = DataInputStream(u.openStream())

                val buffer = ByteArray(contentLength)
                stream.readFully(buffer)
                stream.close()

                val fos = DataOutputStream(FileOutputStream(file))
                fos.write(buffer)
                fos.flush()
                fos.close()

                return file
            }

            override fun onPostExecute(result: File) {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "video/*"
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(result))
                //intent.putExtra(Intent.EXTRA_STREAM, savedImageURI)
                context.startActivity(Intent.createChooser(intent, "Share"))
            }
        }
        asyncTask.execute(videoPath)


    }
}