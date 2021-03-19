package com.sovnem.bisheng;

import android.util.ArrayMap;
import android.util.SparseArray;

public interface IAdapterMap {
    ArrayMap<Class<?>, Integer> getDataToType();

    SparseArray<Class<?>> getTypeToViewHolder();

    ArrayMap<Class<?>, Integer> getViewHolderToLayoutRes();
}
