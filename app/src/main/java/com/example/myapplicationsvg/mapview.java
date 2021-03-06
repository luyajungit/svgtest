package com.example.myapplicationsvg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.PathParser;
import androidx.core.view.GestureDetectorCompat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class mapview extends View {
    public static final String TAG = "mapview";
    private Context context;
    private List<Provice> itemList;
    //    private float scale = 1.3f;//放大倍数1.3倍
//缩放比例
    private float scale = 0f;
    private float mapWidth = 773.0f, mapHeight = 568.0f;
    private Paint paint;//画笔
    //GestureDetector的替代版，存在于v4包中，更兼容更好用的手势识别工具类。
    private GestureDetectorCompat gestureDetectorCompat;
    private int[] colorArray = new int[]{0xFF239BD7, 0xFF30A9E5, 0xFF80CBF1, 0xFFB0D7F8};
    private Provice provinceBeenSelect;//当前选中的省份

    public mapview(Context context) {
        super(context);
    }
    public mapview(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private void init(Context context) {
        this.context = context;
        paint = new Paint();
        paint.setAntiAlias(true);
        loadSVG();

        gestureDetectorCompat = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                handleDown(e.getX(), e.getY());
                Log.d(TAG, "onDown: ");
                return true;
            }
        });
    }

    /**
     * 处理按下事件
     *
     * @param x
     * @param y
     */
    private void handleDown(float x, float y) {
        if (itemList != null) {
            Provice temp = null;
            for (Provice item : itemList) {
                //因为对图像进行放大1.3倍 所以这里需要除以放大系数
                if (item.isSelect((int) (x / scale), (int) (y / scale))) {
                    //判断是否被选中 如果选中跳出循环 拿出被选中的省
                    temp = item;
                    break;
                }
            }
            if (temp != null) {
                provinceBeenSelect = temp;
            }
            postInvalidate();
        }
    }
    //异步读取svg文件
    private void loadSVG() {
        itemList = new ArrayList<>();
//        ThreadPoolUtils.execute(new Runnable() {
//            @Override
//            public void run() {
////                InputStream inputStream = context.getResources().openRawResource(R.raw.chinahigh);
//                InputStream inputStream = context.getResources().openRawResource(R.raw.chinahigh);
//                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();//获取DocumentBuilderFactory
//                DocumentBuilder builder = null;
//
//                try {
//                    builder = factory.newDocumentBuilder();//从factory中获取DocumentBuilder 实例
//                    Document doc = builder.parse(inputStream);
//                    Element rootElement = doc.getDocumentElement();//dom解析
//                    NodeList items = rootElement.getElementsByTagName("path");//把所有包含path的节点拿出来
//
//                    for (int i = 0; i < items.getLength(); i++) {
//                        Element element = (Element) items.item(i);
//                        String pathData = element.getAttribute("android:pathData");//读取path的数据
//                        Path path =  com.example.myapplicationsvg.PathParser.createPathFromPathData(pathData);//通过工具类解析出Path
//                        Provice provinceBeen = new Provice(path);
//                        itemList.add(provinceBeen);
//                        //重绘
//                        handler.sendEmptyMessage(1);
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = context.getResources().openRawResource(R.raw.chinahigh);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();//获取DocumentBuilderFactory
                DocumentBuilder builder = null;

                try {
                    builder = factory.newDocumentBuilder();//从factory中获取DocumentBuilder 实例
                    Document doc = builder.parse(inputStream);
                    Element rootElement = doc.getDocumentElement();//dom解析
                    NodeList items = rootElement.getElementsByTagName("path");//把所有包含path的节点拿出来

                    for (int i = 0; i < items.getLength(); i++) {
                        Element element = (Element) items.item(i);
                        String pathData = element.getAttribute("android:pathData");//读取path的数据
                        Path path =  com.example.myapplicationsvg.PathParser.createPathFromPathData(pathData);//通过工具类解析出Path
                        Provice provinceBeen = new Provice(path);
                        itemList.add(provinceBeen);
                        //重绘
                        handler.sendEmptyMessage(1);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (itemList != null) {
                int totalNumber = itemList.size();
                for (int i = 0; i < totalNumber; i++) {
                    int color = Color.WHITE;
                    //每隔四个省换个颜色 这里随机分配颜色
                    int flag = i % 4;
                    switch (flag) {
                        case 1:
                            color = colorArray[0];
                            break;
                        case 2:
                            color = colorArray[1];
                            break;
                        case 3:
                            color = colorArray[2];
                            break;
                        default:
                            color = colorArray[3];
                            break;
                    }
                    //设置省的颜色
                    itemList.get(i).setDrawColor(color);
                    postInvalidate();//刷新

                }

            }
        }
    };


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (itemList != null) {
            Provice provice = null;
            for ( Provice item : itemList) {
                if (item.isSelect((int) (event.getX() / scale), (int) (event.getY() / scale))) {
                    provice = item;
                    break;
                }
            }
            if (provice != null) {
                provinceBeenSelect = provice;
                postInvalidate();
            }
        }
        return true;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        if (itemList != null) {
//            //先保存下
////            canvas.save();
//            //对图像进行放大1.3倍
//            canvas.scale(scale, scale);
//            //对省进行遍历 把path获取到 然后绘制出来
//            for (Provice provinceBeen : itemList) {
//                //判断遍历的省份是否是当前选中的省份
//                if (provinceBeen != provinceBeenSelect) {
//                    //绘制未选中的省份
//                    provinceBeen.draw(canvas, paint, false);
//                } else {
//                    provinceBeen.draw(canvas, paint, true);
//                }
//            }
//            if (provinceBeenSelect != null) {
//                provinceBeenSelect.draw(canvas, paint, true);
//            }
//        }
        if (itemList != null) {
            canvas.scale(scale, scale);
            for (Provice item : itemList) {
                if (item != provinceBeenSelect) {
                    item.draw(canvas, paint, false);
                }
            }
            if (provinceBeenSelect != null) {
                provinceBeenSelect.draw(canvas, paint, true);
            }
        }
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        scale = Math.min(width/mapWidth, height/mapHeight);

    }

}
