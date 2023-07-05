package com.example.diary;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.UUID;


import static com.example.diary.DatabaseHelper.TABLE_NAME;
import static com.example.diary.MainActivity.TAG_INSERT;
import static com.example.diary.MainActivity.TAG_UPDATE;
import static com.example.diary.MainActivity.dbHelper;
import static com.example.diary.MainActivity.getDbHelper;

public class Detail extends  AppCompatActivity {

    private SQLiteDatabase db;
    EditText title;  //标题
    EditText author; //作者
    TextView time;  //时间
    EditText content;//内容
    Button pictureChoice;//从相册选择照片

    ImageView picture;//照片
    //public DatabaseHelper deHelper= getDbHelper();
    private int tag;
    private int id;
    private static final int CHOICE_PHOTO=2;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
    Date date = new Date(System.currentTimeMillis());

    //通过Random()类生成数组命名
    Random random = new Random();
    String fileName = String.valueOf(random.nextInt(Integer.MAX_VALUE));

    //内部路径
    String dir = "data/data/com.example.diary/image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        title= findViewById(R.id.detail_title);
        author= findViewById(R.id.detail_author);
        time= findViewById(R.id.detail_time);
        content= findViewById(R.id.detail_content);
        pictureChoice=findViewById(R.id.detail_pictureChoice);
        picture=findViewById(R.id.detail_picture);


        title.setSelection(title.getText().length());
        SharedPreferences pref = getSharedPreferences("LoginData",MODE_PRIVATE);
        String name = pref.getString("username","");

        if (name.equals("")){
            author.setText("admin");
        }else {
            author.setText(name);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        time.setText(simpleDateFormat.format(date));
        content.setSelection(content.getText().length());

        pictureChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(Detail.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Detail.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }
            }
        });

        db= dbHelper.getWritableDatabase();
        Intent intent=getIntent();
        tag=intent.getIntExtra("TAG",-1);
        switch(tag){
            case TAG_INSERT:
                break;
            case TAG_UPDATE:
                id=intent.getIntExtra("ID",-1);
                Cursor cursor=db.query(TABLE_NAME,null,"id=?",
                        new String[]{String.valueOf(id)},null,null,null);
                if(cursor.moveToFirst()){
                    String select_title=cursor.getString(cursor.getColumnIndex("title"));
                    String select_author=cursor.getString(cursor.getColumnIndex("author"));
                    String select_content=cursor.getString(cursor.getColumnIndex("content"));
                    title.setText(select_title);
                    author.setText(select_author);
                    content.setText(select_content);

                    Bitmap bitmap;
                    if (!cursor.getString(cursor.getColumnIndex("picture")).equals("")){
                        File file = new File(dir +cursor.getString(cursor.getColumnIndex("picture")));

                        try {
                            FileInputStream input = new FileInputStream(file);
                            bitmap = BitmapFactory.decodeStream(input);
                            input.close();
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        picture.setImageBitmap(bitmap);
                    }
                }
                break;
            default:
        }
    }

    //打开系统相册
    private  void openAlbum(){
        // Intent  intent = new Intent("android.intent.action.GET_CONTENT");
        Intent  intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,CHOICE_PHOTO);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
    //获取返回来的图片
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent  data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOICE_PHOTO){
            if (data != null) {
                //获取数据
                //获取内容解析者对象
                try {
                    Bitmap mBitmap = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(data.getData()));
                    picture.setImageBitmap(mBitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    //将menu中的actionbar添加进来
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    //设置“保存”或者“删除”按钮点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.save:
                if(tag==TAG_INSERT) {
                    //Toast.makeText(this, "开始了", Toast.LENGTH_SHORT).show();
                    ContentValues values = new ContentValues();
                    values.put("title", title.getText().toString());
                    values.put("author", author.getText().toString());
                    values.put("content", content.getText().toString());
                    values.put("time",simpleDateFormat.format(date));
                    final ByteArrayOutputStream os = new ByteArrayOutputStream();

                    if (((BitmapDrawable)picture.getDrawable()) != null){
                        Bitmap bitmap = ((BitmapDrawable)picture.getDrawable()).getBitmap();

                        try {
                            File file = new File(dir + fileName + ".jpg");
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            values.put("picture",fileName + ".jpg");
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //bitmap.compress(Bitmap.CompressFormat.PNG,100,os);

                    }else if (((BitmapDrawable)picture.getDrawable()) == null){
                        values.put("picture","");
                    }

                    db.insert(TABLE_NAME, null, values);
                    values.clear();
                    Toast.makeText(this, "Save  succeed", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                }else if(tag==TAG_UPDATE){
                    //修改title、content和picture
                    String update_title=title.getText().toString();
                    String update_author=author.getText().toString();
                    String update_content=content.getText().toString();
                    ContentValues values=new ContentValues();
                    values.put("title",update_title);
                    values.put("author",update_author);
                    values.put("content",update_content);
                    values.put("time",simpleDateFormat.format(date));
                    final ByteArrayOutputStream os = new ByteArrayOutputStream();

                    if (((BitmapDrawable)picture.getDrawable()) != null){
                        Bitmap bitmap = ((BitmapDrawable)picture.getDrawable()).getBitmap();

                        try {
                            File file = new File(dir + fileName + ".jpg");
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            values.put("picture",fileName + ".jpg");
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        //bitmap.compress(Bitmap.CompressFormat.PNG,100,os);

                    }else if (((BitmapDrawable)picture.getDrawable()) == null){
                        values.put("picture","");
                    }
//                    Bitmap bitmap = ((BitmapDrawable)picture.getDrawable()).getBitmap();
//                    bitmap.compress(Bitmap.CompressFormat.PNG,100,os);
//                    values.put("picture",os.toByteArray());
                    db.update(TABLE_NAME,values,"id=?",new String[]{String.valueOf(id)});
                    finish();
                    break;
                }
            case R.id.delete:
                if(tag==TAG_UPDATE) {
                    db.delete(TABLE_NAME,"id=?",new String[]{String.valueOf(id)});
                }
                Toast.makeText(this,"Delete",Toast.LENGTH_SHORT).show();
                finish();
                break;
            case R.id.goBack:
                finish();
                break;
            default:
        }
        return true;
    }
}
