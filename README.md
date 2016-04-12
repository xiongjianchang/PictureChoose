# PictureChoose
Choose from camera or photo album pictures。图片选择来源于相机或相册。
主要应用于类似选择、修改头像，和有简单的图片上传需求。


### 鸣谢
项目是在[chcdg](https://github.com/chcdg)大神的代码上进行完善的，关注大神更多项目请猛戳[这里](https://github.com/chcdg)。


### 特色
* 以工具类的形式集成于项目里面
* 直接实例化对象，并设置其属性就定制选择图片的需求
* 支持相机拍照和选择相册照片
* 支持图片的裁剪和压缩
* 目前相册选择只支持选择单张


### 用法

```java
dependencies {
 compile 'com.xjc.master.picturechoose:sourcelibrary:1.0.1@aar'
}
```

### 声明
在AndroidManifest.xml文件中,找到要实现选择图片的Activity中声明：

```java
 <activity
            android:name=".MainActivity"
            android:configChanges="orientation|screenSize|keyboardHidden"/>
            <!--因为调用相机会引发横竖屏切换问题，所以这里监听属性变化，避免activity生命周期重建-->
```

### 创建对象
在Activity中实例化图片选择器PictureChooser，可以通过方法定义要实现的需求：

```java
  PictureChooser pictureChooser = new PictureChooser(mContext);
  //设置图片名称，会做存储
  pictureChooser.setCameraPicName("headPhoto.jpg");
  //是否裁剪，裁剪的比例和宽高
  pictureChooser.setIsClip(true, 1, 1, 0, 0);
  //是否压缩，压缩后的最大质量（单位kb）和图片宽高
  pictureChooser.setIsCompressor(true, 500, 240, 240);
```

如果是要用相机拍照的话，就设置选择相机：

```java
  //设置选择相机
  pictureChooser.setmPictureFrom(PictureChooser.PictureFrom.CAMERA);
```

要用相册里面的图片，就设置选择相册：

```java
  //设置选择相册
  pictureChooser.setmPictureFrom(PictureChooser.PictureFrom.GALLERY);
```

还要记得设置选完图片后的回调事件处理，实现PictureChooser类里的OnPicturePickListener接口：

```java
  pictureChooser.execute(new PictureChooser.OnPicturePickListener() {
                    /**
                     * 这里是获取到图片路径
                     * @param filePath 选中的图片在手机文件系统里的路径
                     */
                    @Override
                    public void senFile(String filePath) {

                    }

                    /**
                     * 如果有做压缩处理，这里会返回压缩后的路径
                     * @param filePath 选中的图片在手机文件系统里的路径
                     */
                    @Override
                    public void compressorSuccess(String filePath) {

                    }
                });
```

最后一步，也是最容易忽略的，加上onActivityResult的调用，因为是封装成工具类，所以要获取拍照和裁剪后的数据得靠onActivityResult方法，把data值传给pictureChooser接着处理：

```java
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      //要做页面回调的响应处理，把操作的数据传给图片选择操作类
      pictureChooser.onActivityResult(requestCode, resultCode, data);
  }
```

### 具体用法案例
这里直接粘完整的代码，大家都是大神，这种小代码一看就懂。

```java
/**
 * ProjectName: PictureChoose
 * Describe: 图片选择器操作案例
 * Author: 熊建昌
 * Date: 2016/3/16 10:20
 * Email: jianchang1230@163.com
 * QQ: 939635660
 * Copyright (c) 2016, *******.com All Rights Reserved.
 */
public class MainActivity extends AppCompatActivity {

    ImageView imgAddPhoto;

    private String photoPath = null; //记录头像地址，要上传的时候可以将地址读成文件上传

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        setListeners();
    }


    private void findViews() {
        imgAddPhoto = (ImageView) findViewById(R.id.img_add_photo);
    }

    private void setListeners() {
        imgAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChoosePictureDialog();
            }
        });
    }

    PictureChooser pictureChooser;

    /**
     * 显示选择图片的窗口
     */
    private void showChoosePictureDialog() {
        final String[] pictureFrom = {"拍照上传", "相册选择"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择图片来源");
        builder.setItems(pictureFrom, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (null == pictureChooser) {
                    pictureChooser = new PictureChooser(MainActivity.this);
                    //图片名称，会做存储
                    pictureChooser.setCameraPicName("headPhoto.jpg");
                    //是否裁剪，裁剪的比例和宽高
                    pictureChooser.setIsClip(true, 1, 1, 0, 0);
                    //是否压缩，压缩后的最大质量（单位kb）和图片宽高
                    pictureChooser.setIsCompressor(true, 500, 240, 240);
                }
                if (0 == which) {
                    //设置选择相机
                    pictureChooser.setmPictureFrom(PictureChooser.PictureFrom.CAMERA);
                }
                if (1 == which) {
                    //设置选择相册
                    pictureChooser.setmPictureFrom(PictureChooser.PictureFrom.GALLERY);
                }
                pictureChooser.execute(new PictureChooser.OnPicturePickListener() {
                    /**
                     * 这里是获取到图片路径
                     * @param filePath 选中的图片在手机文件系统里的路径
                     */
                    @Override
                    public void senFile(String filePath) {
                        //这里改成自己的图片加载框架，我只是简单做一下案例
                        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                        imgAddPhoto.setImageBitmap(bitmap);
                        //把图片路径存储起来，上传的时候要用到
                        photoPath = filePath;
                    }

                    /**
                     * 如果有做压缩处理，这里会返回压缩后的路径
                     * @param filePath 选中的图片在手机文件系统里的路径
                     */
                    @Override
                    public void compressorSuccess(String filePath) {
                        photoPath = filePath;
                    }
                });
            }
        });
        builder.setNegativeButton("取消", null);
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //要做页面回调的响应处理，把操作的数据传给图片选择操作类
        pictureChooser.onActivityResult(requestCode, resultCode, data);
    }
}

```


