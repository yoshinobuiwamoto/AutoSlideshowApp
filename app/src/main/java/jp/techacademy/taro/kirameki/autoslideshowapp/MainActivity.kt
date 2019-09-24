package jp.techacademy.taro.kirameki.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.os.Handler
import android.view.View
import jp.techacademy.taro.kirameki.autoslideshowapp.R
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100

    private var mTimer: Timer? = null
    private var cursor: Cursor? = null
    private var mHandler = Handler()

    private var AutoStopFlag = "STOP"    // 自動送り／停止のボタンを最初は"STOP"にセット


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsFirst()  // 最初の画像を取得
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsFirst()  // 最初の画像を取得
        }


        ///////////////////////////////////////////////////////////////////////////////////// ボタン　クリック


        //
        //  進むボタンをクリックした
        //
        next_button.setOnClickListener {
            getContentsNext()    // 進む画面を表示
        }
        //
        //  戻るボタンをクリックした
        //
        previous_button.setOnClickListener {
            getContentsPrevious()    // 戻る画面を表示
        }
        //
        //  再生／停止ボタンをクリックした
        //
        auto_stop_button.setOnClickListener {
            if (AutoStopFlag == "STOP") {
                AutoStopFlag = "AUTO"               // 再生ボタンをおした状態にする
                previous_button.isClickable = false   // 戻るボタンを無効にする
                next_button.isClickable = false       // 進むボタンを無効にするの

                if (mTimer == null) {
                    mTimer = Timer()
                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {
                            mHandler.post {
                                getContentsNext()    // つぎの画面を表示

                            }

                        }
                    }, 100, 2000) // 最初に始動させるまで 100ミリ秒、ループの間隔を 2000ミリ秒 に設定
                }
            } else {                                 // すでに再生をしている状態なので停止要求とみなす
                AutoStopFlag = "STOP"            // 停止状態にする
                previous_button.isClickable = true    // 戻るボタンを有効にする
                next_button.isClickable = true        // 進むボタンを有効にする
                if (mTimer != null){
                    mTimer!!.cancel()
                    mTimer = null
                }
                // Activity破棄


            }

        }

    }
    override fun onDestroy() {
        super.onDestroy()

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsFirst()  // 最初の画像を取得
                }
        }
    }



    //////////////////////////////////////////////////////////////////////////////////////   共通　関数

    //
    //    最初の画像を取得
    //
    private fun getContentsFirst() {       // 最初の画像を取得
        // 画像の情報を取得する
        val resolver = contentResolver
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        )


        if (cursor!! != null && cursor!!.moveToFirst()) {

            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor!!.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            imageView.setImageURI(imageUri)

        }
       // cursor.close()

    }

    //
    //    次の画像を取得
    //
    private fun getContentsNext() {         // 次の画像を取得
        // 画像の情報を取得する

        if (cursor!! != null && cursor!!.moveToNext()) {

            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor!!.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            imageView.setImageURI(imageUri)
        } else {
            if (cursor!!.moveToFirst()) {   // 次の画像を取得しようとしたらFalseだったので最初の画像をget

                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                imageView.setImageURI(imageUri)

            }

        }

       // cursor.close()

    }

    //
    //  ひとつ前の画像を取得
    //
    private fun getContentsPrevious() {            // ひとつ前の画像を取得
        // 画像の情報を取得する

        if (cursor!! != null && cursor!!.moveToPrevious()) {    // ひとつ前の画像を取得

            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor!!.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            imageView.setImageURI(imageUri)

        } else {
            if (cursor!!.moveToLast()) {

                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                imageView.setImageURI(imageUri)

            }

        }

      //  cursor.close()




    }
}

