package com.chen.barcode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.editText)
    EditText mEditText;
    @BindView(R.id.createbarcode)
    TextView mCreatebarcode;
    @BindView(R.id.activity_barcode)
    ConstraintLayout mActivityBarcode;
    @BindView(R.id.imageView)
    ImageView mImageView;


    private final int MAX_WIDTH = 2;


    int arr[][] = new int[][]{
            {0, 0, 1, 1, 0}, //0
            {1, 0, 0, 0, 1}, //1
            {0, 1, 0, 0, 1}, //2
            {1, 1, 0, 0, 0}, //3
            {0, 0, 1, 0, 1}, //4
            {1, 0, 1, 0, 0}, //5
            {0, 1, 1, 0, 0}, //6
            {0, 0, 0, 1, 1}, //7
            {1, 0, 0, 1, 0}, //8
            {0, 1, 0, 1, 0}};//9


    private ArrayList<Integer> mData = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    public Bitmap canvasDraw(int numLength) {


        int canvasX, canvasStartY, canvasStopY;
        canvasStartY = 0;

        ViewGroup.LayoutParams para;
        para = mImageView.getLayoutParams();
        int vWidth = para.width - DensityUtils.dip2px(getApplicationContext(), 20);

        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();

        canvasX = screenWidth - vWidth > 0 ? (screenWidth - vWidth) / 2 : 0;

        //条码高度
        canvasStopY = para.height;


        //总共绘制的条码数，以最窄为基础，宽码换算成窄码
        int sum = 4 + numLength * (3 + 2 * MAX_WIDTH) + MAX_WIDTH + 2;

        //单个条码的宽度
        int singleWidth = vWidth / sum;


        //创建一个的Bitmap对象
        Bitmap bitmap = Bitmap.createBitmap(vWidth, vWidth / 3, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        //设置颜色
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(false);
        //设置单个条码宽度
        paint.setStrokeWidth((float) singleWidth);

        //绘制起始码
        canvas.drawLine(canvasX, canvasStartY, canvasX, canvasStopY, paint);
        canvasX = canvasX + singleWidth;
        canvasX = canvasX + singleWidth;
        canvas.drawLine(canvasX, canvasStartY, canvasX, canvasStopY, paint);
        canvasX = canvasX + singleWidth;
        canvasX = canvasX + singleWidth;
        paint.setColor(Color.BLACK);

        for (int i = 0; i < numLength; i = i + 2) {

            //获取奇数数字
            int mNumFirst = mData.get(i);
            //获取偶数数字
            int mNumSecond = mData.get(i + 1);


            for (int j = 0; j < 5; j++) {
                //奇数数字对应码
                int firstIndex = arr[mNumFirst][j];
                //偶数数字对应码
                int secondIndex = arr[mNumSecond][j];

                if (firstIndex == 0) {
                    //绘制窄条
                    canvas.drawLine(canvasX, canvasStartY, canvasX, canvasStopY, paint);
                    canvasX = canvasX + singleWidth;
                } else {
                    //绘制宽条
                    for (int k = 0; k < MAX_WIDTH; k++) {
                        canvas.drawLine(canvasX, canvasStartY, canvasX, canvasStopY, paint);
                        canvasX = canvasX + singleWidth;
                    }

                }
                //修改坐标，对应白色空白宽窄
                canvasX = canvasX + singleWidth + singleWidth * (MAX_WIDTH - 1) * secondIndex;
            }


        }

        //绘制结束符
        canvas.drawLine(canvasX, canvasStartY, canvasX, canvasStopY, paint);
        canvasX = canvasX + singleWidth;
        canvas.drawLine(canvasX, canvasStartY, canvasX, canvasStopY, paint);
        canvasX = canvasX + singleWidth;
        canvasX = canvasX + singleWidth;
        canvas.drawLine(canvasX, canvasStartY, canvasX, canvasStopY, paint);


        //保存canvas之前的操作,在sava()和restore之间的操作不会对canvas之前的操作进行影响
        canvas.save();

        return bitmap;

    }


    @OnClick(R.id.createbarcode)
    public void createBarcode() {


        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {

                String cont = mEditText.getText().toString();
//                String cont ="01234567890";

                if (cont.length() % 2 == 1)
                    cont = "0" + cont;
                for (int i = 0; i < cont.length(); i++) {
                    int num = Integer.valueOf(cont.substring(i, i + 1));
                    e.onNext(num);
                }
                e.onComplete();
            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer s) {
                mData.add(s);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

                for (Integer con : mData)
                    LogUtils.e("" + con);
            }
        });


        Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> e) throws Exception {
                e.onNext(canvasDraw(mData.size()));
                e.onComplete();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        mImageView.setImageBitmap(bitmap);
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.e("onError");
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        LogUtils.e("onComplete");
                        mData.clear();
                    }
                });

    }
}
