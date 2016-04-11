# PictureChoose
Choose from camera or photo album pictures。图片选择来源于相机或相册。


##特色
* 以工具类的形式集成于项目里面
* 直接实例化对象，并设置其属性就定制选择的需求
* 支持相机拍照和选择相册照片
* 支持图片的裁剪和压缩
* 目前相册选择只支持选择单张


## 用法

```java
dependencies {
 compile 'com.xjc.master.picturechoose:sourcelibrary:1.0.0@aar'
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


###具体用法
具体怎么用我就不描述了，直接粘代码，大家都是大神，这种小代码一看就懂。

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


