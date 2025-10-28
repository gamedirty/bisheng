package com.sovnem.bisheng;

import android.util.ArrayMap;
import android.util.SparseArray;

/**
 * 空实现 - 示例项目使用运行时反射注册所有类型
 * 实际项目中，此类会由注解处理器自动生成
 */
public class BiShengAdapterMapImpl implements IAdapterMap {
    
    @Override
    public ArrayMap<Class<?>, Integer> getDataToType() {
        return new ArrayMap<>();
    }
    
    @Override
    public SparseArray<Class<?>> getTypeToViewHolder() {
        return new SparseArray<>();
    }
    
    @Override
    public ArrayMap<Class<?>, Integer> getViewHolderToLayoutRes() {
        return new ArrayMap<>();
    }
}

