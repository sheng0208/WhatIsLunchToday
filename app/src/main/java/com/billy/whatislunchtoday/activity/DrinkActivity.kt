package com.billy.whatislunchtoday.activity

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.billy.whatislunchtoday.R
import com.billy.whatislunchtoday.model.FireBaseModel
import com.billy.whatislunchtoday.model.FireBaseStorageModel
import kotlinx.android.synthetic.main.activity_drink.*
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.R.attr.scaleHeight
import android.R.attr.scaleWidth
import android.graphics.Matrix


/**
 * Created by billylu on 2017/10/30.
 */
class DrinkActivity : BaseActivity() {
    private val TAG = javaClass.simpleName

    private val PICK_FROM_GALLERY = 100

    private lateinit var progress : ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drink)

        setUpToolbar()
        setUpFab()

        FireBaseModel().readData("drink", object : FireBaseModel.FireBaseCallBack{
            override fun onGetData(list: List<*>) {

            }
        })

    }

    private fun setUpFab() {
        fab.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, PICK_FROM_GALLERY)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FROM_GALLERY && data != null){
            var uri = data.data
            var cr = this.contentResolver

            var bitmap = reBitmapSize(BitmapFactory.decodeStream(cr.openInputStream(uri)))

            Log.i(TAG, "bitmap: " + bitmap.height +" , "+  bitmap.width)
            var view = LayoutInflater.from(this@DrinkActivity).inflate(R.layout.select_img_layout, null)
            var imgView = view.findViewById<ImageView>(R.id.selected_img)


            imgView.setImageBitmap(bitmap)

            showDialog(view, uri)
        }

    }



    private fun showDialog(view : View, uri : Uri) {
        AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("上傳", object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        var name_editText = view.findViewById<EditText>(R.id.name_editText)
                        var storeName = name_editText.text.toString()
                        progress = ProgressDialog.show(this@DrinkActivity, "", "上傳中...")
                        FireBaseStorageModel().uploadImg("drink", uri, object : FireBaseStorageModel.UploadCallBack {
                            override fun onSuccess(uri: Uri) {
                                progress.dismiss()
                                Toast.makeText(this@DrinkActivity, "上傳成功", Toast.LENGTH_SHORT).show()
                                insertImgInfo(uri, storeName)
                            }
                        })
                    }
                })
                .setNegativeButton("取消", null)
                .show()
    }

    private fun insertImgInfo(uri: Uri, storeName: String) {
        var map = HashMap<String, String>();
        map.put("storeName", storeName)
        map.put("imgUri", "" + uri)
        FireBaseModel().saveMapData("drink", map)
    }

    private fun reBitmapSize(bitmap: Bitmap) : Bitmap {
        var width = bitmap.width
        var height = bitmap.height
        var newWidth = 320
        var newHeight = 480

        if (bitmap.width < newWidth && bitmap.height < newHeight){
            return bitmap
        }

        var scaleWidth = newWidth.toFloat() / width.toFloat()
        var scaleHeight = newHeight.toFloat() / height.toFloat()

        var matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)

        val newbm = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,
                true)
        return newbm
    }




//    class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
//
//        inner class ViewHolder(view : View) : RecyclerView.ViewHolder() {
//            var drink_text : TextView
//            var drink_img : ImageView
//
//            init {
//                drink_img = view.findViewById(R.id.drink_img)
//                drink_text = view.findViewById(R.id.drink_text)
//            }
//
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyAdapter.ViewHolder {
//            var view = LayoutInflater.from(parent!!.context).inflate(R.layout.drink_list_item, parent, false)
//            var viewHolder = ViewHolder(view)
//
//            return viewHolder
//        }
//
//
//
//        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
//
//        }
//
//        override fun getItemCount(): Int {
//
//
//            return 0;
//        }
//
//
//
//    }
}