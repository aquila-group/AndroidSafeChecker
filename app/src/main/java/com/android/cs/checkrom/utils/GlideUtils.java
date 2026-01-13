package com.android.cs.checkrom.utils;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 描述：GlideUtils
 * 作者：孟崔广
 * 时间：2018/1/10 17:51
 * 邮箱：mengcga@163.com
 */
public class GlideUtils {
    /**
     * Glide特点
     * 使用简单
     * 可配置度高，自适应程度高
     * 支持常见图片格式 Jpg png gif webp
     * 支持多种数据源  网络、本地、资源、Assets 等
     * 高效缓存策略    支持Memory和Disk图片缓存 默认Bitmap格式采用RGB_565内存使用至少减少一半
     * 生命周期集成   根据Activity/Fragment生命周期自动管理请求
     * 高效处理Bitmap  使用Bitmap Pool使Bitmap复用，主动调用recycle回收需要回收的Bitmap，减小系统回收压力
     * 这里默认支持Context，Glide支持Context,Activity,Fragment，FragmentActivity
     */

    //默认加载
    public static void loadImageView(Context mContext, Object path, ImageView mImageView) {
        Glide.with(mContext).load(path).into(mImageView);
    }

    //设置加载中图片
    public static void loadImageViewLoding(Context mContext, String path, ImageView mImageView, int lodingImage) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(lodingImage);
        Glide.with(mContext).load(path).apply(requestOptions).into(mImageView);
    }

    //设置加载中以及加载失败图片
    public static void loadImageViewLoding(Context mContext, Object path, ImageView mImageView, int lodingImage, int errorImageView) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(lodingImage);
        requestOptions.error(errorImageView);
        Glide.with(mContext).load(path).apply(requestOptions).into(mImageView);
    }

    //设置加载中以及加载失败圆形图片
    public static void loadCircleImageView(Context mContext, String path, ImageView mImageView, int lodingImage, int errorImageView) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(lodingImage);
        requestOptions.error(errorImageView);
        requestOptions.circleCrop();
        Glide.with(mContext).load(path).apply(requestOptions).into(mImageView);
    }

    //加载Gif
    public static void loadImageViewGif(Context mContext, int drawable, ImageView imageView) {
        Glide.with(mContext).clear(imageView);
        Glide.with(mContext).load(drawable).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
    }

    //加载Gif
    public static void loadImageViewBitMap(Context mContext, int drawable, ImageView imageView) {
        Glide.with(mContext).asBitmap().load(drawable).into(imageView);
    }

    //加载Gif
    public static void loadImageViewGifUrl(Context mContext, ImageView view, String url) {
        RequestOptions options = (new RequestOptions())
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .timeout(60000)
                .signature(new ObjectKey(System.currentTimeMillis()))
                .priority(Priority.HIGH);
        Glide.with(mContext).load(url).apply(options).into(view);
    }

    //只加载一次Gif
    public static void loadOneTimeGif(Context context, Object model, final ImageView imageView, final GifListener gifListener) {
        Glide.with(context).asGif().load(model).listener(new RequestListener<GifDrawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                try {
                    Field gifStateField = GifDrawable.class.getDeclaredField("state");
                    gifStateField.setAccessible(true);
                    Class gifStateClass = Class.forName("com.bumptech.glide.load.resource.gif.GifDrawable$GifState");
                    Field gifFrameLoaderField = gifStateClass.getDeclaredField("frameLoader");
                    gifFrameLoaderField.setAccessible(true);
                    Class gifFrameLoaderClass = Class.forName("com.bumptech.glide.load.resource.gif.GifFrameLoader");
                    Field gifDecoderField = gifFrameLoaderClass.getDeclaredField("gifDecoder");
                    gifDecoderField.setAccessible(true);
                    Class gifDecoderClass = Class.forName("com.bumptech.glide.gifdecoder.GifDecoder");
                    Object gifDecoder = gifDecoderField.get(gifFrameLoaderField.get(gifStateField.get(resource)));
                    Method getDelayMethod = gifDecoderClass.getDeclaredMethod("getDelay", int.class);
                    getDelayMethod.setAccessible(true);
                    //设置只播放一次
                    resource.setLoopCount(1);
                    //获得总帧数
                    int count = resource.getFrameCount();
                    int delay = 0;
                    for (int i = 0; i < count; i++) {
                        //计算每一帧所需要的时间进行累加
                        delay += (int) getDelayMethod.invoke(gifDecoder, i);
                    }
                    imageView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (gifListener != null) {
                                gifListener.gifPlayComplete();
                            }
                        }
                    }, delay);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                return false;
            }
        }).into(imageView);
    }

    /**
     * Gif播放完毕回调
     */
    public interface GifListener {
        void gifPlayComplete();
    }

    public static void clear(Context context,ImageView iv){
        if(iv!=null){
            Glide.with(context).clear(iv);
        }
    }
}
