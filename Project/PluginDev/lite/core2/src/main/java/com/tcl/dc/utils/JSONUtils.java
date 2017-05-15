//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.utils;

import com.tcl.dc.PLog;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {
    public JSONUtils() {}

    public static JSONObject toJson(Object obj) throws JSONException {
        if (obj == null) {
            return null;
        } else {
            Class<?> cla = obj.getClass();
            Field[] fields = cla.getDeclaredFields();
            JSONObject out = new JSONObject();
            Field[] var4 = fields;
            int var5 = fields.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                Field field = var4[var6];
                if (!Modifier.isStatic(field.getModifiers())) {
                    if ((field.getModifiers() & 1) == 0) {
                        field.setAccessible(true);
                    }

                    try {
                        if (field.getType() == Long.TYPE) {
                            out.put(field.getName(), field.getLong(obj));
                        } else if (field.getType() == Double.TYPE) {
                            out.put(field.getName(), field.getDouble(obj));
                        } else if (field.getType() == Float.TYPE) {
                            out.put(field.getName(), (double) field.getFloat(obj));
                        } else if (field.getType() == Integer.TYPE) {
                            out.put(field.getName(), field.getInt(obj));
                        } else if (field.getType() == Boolean.TYPE) {
                            out.put(field.getName(), field.getBoolean(obj));
                        } else if (field.getType() != Integer.class && field.getType() != Boolean.class
                                && field.getType() != Double.class && field.getType() != Float.class
                                && field.getType() != Long.class) {
                            if (field.getType() == String.class) {
                                out.put(field.getName(), field.get(obj));
                            } else if (field.getType() == List.class) {
                                JSONArray array = toArray((List) field.get(obj));
                                out.put(field.getName(), array);
                            } else {
                                JSONObject data;
                                if (field.getType() == Map.class) {
                                    Map map = (Map) field.get(obj);
                                    if (map != null) {
                                        data = new JSONObject(map);
                                        out.put(field.getName(), data);
                                    }
                                } else {
                                    data = (JSONObject) field.get(obj);
                                    if (data != null) {
                                        data = toJson(data);
                                        out.put(field.getName(), data);
                                    }
                                }
                            }
                        } else {
                            out.put(field.getName(), field.get(obj));
                        }
                    } catch (IllegalAccessException var10) {
                        PLog.printStackTrace(var10);
                    }
                }
            }

            return out;
        }
    }

    public static JSONArray toArray(List<?> list) throws JSONException {
        JSONArray out = new JSONArray();
        if (list != null && list.size() != 0) {
            Iterator var2 = list.iterator();

            while (var2.hasNext()) {
                Object o = var2.next();
                out.put(toJson(o));
            }

            return out;
        } else {
            return out;
        }
    }

    public static String toJSONString(Object o) throws JSONException {
        JSONObject jsonObject = toJson(o);
        return jsonObject == null ? null : jsonObject.toString();
    }
}
