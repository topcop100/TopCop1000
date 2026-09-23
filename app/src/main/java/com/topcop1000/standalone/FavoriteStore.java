package com.topcop1000.standalone;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.*;

public final class FavoriteStore {
    private static final String PREF="topcop_favorites";
    private FavoriteStore(){}

    public static void add(Context c,String category,String value){
        SharedPreferences p=c.getSharedPreferences(PREF,Context.MODE_PRIVATE);
        Set<String> s=new LinkedHashSet<>(p.getStringSet(category,Collections.emptySet()));
        s.add(value); p.edit().putStringSet(category,s).apply();
    }

    public static void remove(Context c,String category,String value){
        SharedPreferences p=c.getSharedPreferences(PREF,Context.MODE_PRIVATE);
        Set<String> s=new LinkedHashSet<>(p.getStringSet(category,Collections.emptySet()));
        s.remove(value); p.edit().putStringSet(category,s).apply();
    }

    public static ArrayList<String> list(Context c,String category){
        return new ArrayList<>(c.getSharedPreferences(PREF,Context.MODE_PRIVATE).getStringSet(category,Collections.emptySet()));
    }
}
