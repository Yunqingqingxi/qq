package com.example.qq.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

/**
 * Glide配置类
 * 用于自定义Glide图片加载库的全局配置，包括缓存大小、图片质量等
 */
@GlideModule
public class GlideConfig extends AppGlideModule {
    
    /**
     * 应用Glide配置选项
     * 配置内存缓存、磁盘缓存大小，以及图片加载的默认选项
     *
     * @param context 应用程序上下文
     * @param builder Glide配置构建器
     */
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {

        /**
         * 设置内存缓存大小为20MB
         * 使用LruResourceCache实现最近最少使用算法
         */
        builder.setMemoryCache(new LruResourceCache(1024 * 1024 * 20));
        
        /**
         * 设置磁盘缓存大小为100MB
         * 使用内部存储空间作为缓存位置
         */
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, 1024 * 1024 * 100));

        /**
         * 设置默认的图片加载选项
         * - 使用RGB_565格式解码图片，相比ARGB_8888能节省一半内存
         * - 使用RESOURCE策略缓存处理后的图片资源
         */
        builder.setDefaultRequestOptions(new RequestOptions()
            .format(DecodeFormat.PREFER_RGB_565)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE));
    }
} 