package com.billy.whatislunchtoday.activity

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.billy.whatislunchtoday.R
import com.billy.whatislunchtoday.bean.Photo
import com.billy.whatislunchtoday.model.FireBaseModel
import com.billy.whatislunchtoday.model.FireBaseStorageModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_food.*

/**
 * Created by billylu on 2017/10/30.
 */
class FoodListActivity : BaseActivity() {
    private val TAG = javaClass.simpleName

    private val PICK_FROM_GALLERY = 100

    private lateinit var progress : ProgressDialog
    private lateinit var myAdapter : MyAdapter
    private lateinit var recycleView : RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_food)

        recycleView = findViewById(R.id.food_recyclerView)

        setUpToolbar()
        setUpFab()

        FireBaseModel().readData("food", object : FireBaseModel.FireBaseCallBack{
            override fun onGetData(list: List<Photo>) {
                Log.i(TAG, "" + list.size)
                var photo = list.get(0).getStoreName()
                Log.i(TAG, photo)

                myAdapter = MyAdapter(this@FoodListActivity, list)
                recycleView.layoutManager = GridLayoutManager(this@FoodListActivity, 1)
                recycleView.adapter = myAdapter
            }

            override fun onNullData(msg: String) {
                Toast.makeText(this@FoodListActivity, msg, Toast.LENGTH_SHORT).show()
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
            var view = LayoutInflater.from(this@FoodListActivity).inflate(R.layout.select_img_layout, null)
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
                        progress = ProgressDialog.show(this@FoodListActivity, "", "上傳中...")
                        FireBaseStorageModel().uploadImg("food", uri, object : FireBaseStorageModel.UploadCallBack {
                            override fun onSuccess(uri: Uri) {
                                progress.dismiss()
                                Toast.makeText(this@FoodListActivity, "上傳成功", Toast.LENGTH_SHORT).show()
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
        FireBaseModel().saveMapData("food", map)
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

    private fun setUpFab() {
        food_fab.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent, PICK_FROM_GALLERY)
            }
        })
    }



    class MyAdapter(context : Context,list: List<Photo>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        private var mContext : Context
        private var photoList : List<Photo>

        init {
            mContext = context
            photoList = list
        }

        class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
            var food_text : TextView
            var food_img : ImageView
            init {
                food_img = itemView!!.findViewById(R.id.food_img)
                food_text = itemView!!.findViewById(R.id.food_text)
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyAdapter.ViewHolder {
            var view = LayoutInflater.from(parent!!.context).inflate(R.layout.food_list_item, parent, false)
            var viewHolder = ViewHolder(view)

            return viewHolder
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            var photo = photoList.get(position)
            holder!!.food_text.setText(photo.getStoreName())
            holder.food_img.setDrawingCacheEnabled(true);
            Glide.with(holder.itemView)
                    .load(photo.getImgUri())
                    .into(holder.food_img)

        }

        override fun getItemCount(): Int {

            return photoList.size
        }
    }
}