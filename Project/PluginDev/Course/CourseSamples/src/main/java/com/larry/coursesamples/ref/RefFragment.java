package com.larry.coursesamples.ref;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.larry.light.IAdapterListener;
import com.larry.light.LightRecycleViewFragment;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Larry on 2017/4/3.
 */

public class RefFragment extends LightRecycleViewFragment implements IAdapterListener<RefInfo> {

    private RefAdapter mRefAdapter;

    @Override
    protected RefAdapter getAdapter() {

        if (mRefAdapter == null) {
            mRefAdapter = new RefAdapter(getActivity());
            mRefAdapter.setAdapterListener(this);
        }
        return mRefAdapter;
    }

    @Override
    public int getTitle() {
        return 0;
    }

    @Override
    protected void constructAndPerformRequest(boolean clearOnAdd, boolean readCache, int index) {
        super.constructAndPerformRequest(clearOnAdd, readCache, index);
        if (clearOnAdd) {
            getAdapter().clearItem();
        }
        getAdapter().addItem(getDatas());
        getAdapter().notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, RefInfo refInfo, int position) {

        if (position == 0) {
            Method m = null;
            try {
                Class c = Class.forName("android.app.ActivityManager");
                m = c.getMethod("forceStopPackage", String.class);
                m.invoke(getActivity().getPackageManager(), "com.larry.coursesamples");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else if (position == 1) {
            Nexus nexus = new Nexus();
            System.out.println(nexus.getClass().getName());
        } else if (position == 2) {

            Class<?> class1 = null;
            Class<?> class2 = null;
            Class<?> class3 = null;
            // 一般采用这种形式
            try {

                class1 = Class.forName("com.larry.coursesamples.ref.Glexy");
                System.out.println("类名称   " + class1.getName());

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            class2 = new Glexy().getClass();
            class3 = Glexy.class;

            System.out.println("类名称   " + class2.getName());
            System.out.println("类名称   " + class3.getName());


        } else if (position == 3) {

            try {
                Class<?> clazz = Class.forName("com.larry.coursesamples.ref.Phone");

                // 取得父类
                Class<?> parentClass = clazz.getSuperclass();
                System.out.println("clazz的父类为：" + parentClass.getName());

                // 获取所有的接口
                Class<?> intes[] = clazz.getInterfaces();
                System.out.println("clazz实现的接口有：");
                for (int i = 0; i < intes.length; i++) {
                    System.out.println((i + 1) + "：" + intes[i].getName());
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } else if (position == 4) {

            try {
                Class<?> class1 = Class.forName("com.larry.coursesamples.ref.Glexy");

                // 取得全部的构造函数 使用构造函数赋值
                Constructor<?> cons[] = class1.getConstructors();
                // 查看每个构造方法需要的参数
                for (int i = 0; i < cons.length; i++) {
                    Class<?> clazzs[] = cons[i].getParameterTypes();
                    System.out.print("cons[" + i + "] (");
                    for (int j = 0; j < clazzs.length; j++) {
                        if (j == clazzs.length - 1)
                            System.out.print(clazzs[j].getName());
                        else
                            System.out.print(clazzs[j].getName() + ",");
                    }
                    System.out.println(")");
                }


            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } else if (position == 5) {

            try {
                Class<?> class1 = Class.forName("com.larry.coursesamples.ref.Glexy");
                Glexy glexy = (Glexy) class1.newInstance();
                glexy.setPrice(5888);
                glexy.setName("Glexy 7");
                System.out.println(glexy);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            }

        } else if (position == 6) {
            try {
                Class<?> clazz = Class.forName("com.larry.coursesamples.ref.Nexus");

                System.out.println("Nexus：");
                // 取得本类的全部属性
                Field[] field = clazz.getDeclaredFields();
                for (int i = 0; i < field.length; i++) {
                    // 权限修饰符
                    int mo = field[i].getModifiers();
                    String priv = Modifier.toString(mo);
                    // 属性类型
                    Class<?> type = field[i].getType();
                    System.out.println(priv + " " + type.getName() + " " + field[i].getName() + ";");
                }

                System.out.println("Nexus的父类：");
                // 取得实现的接口或者父类的属性
                Field[] filed1 = clazz.getFields();
                for (int j = 0; j < filed1.length; j++) {
                    // 权限修饰符
                    int mo = filed1[j].getModifiers();
                    String priv = Modifier.toString(mo);
                    // 属性类型
                    Class<?> type = filed1[j].getType();
                    System.out.println(priv + " " + type.getName() + " " + filed1[j].getName() + ";");
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if (position == 7) {
            try {
                Class<?> clazz = Class.forName("com.larry.coursesamples.ref.Nexus");
                Method method[] = clazz.getMethods();
                for (int i = 0; i < method.length; ++i) {
                    Class<?> returnType = method[i].getReturnType();
                    Class<?> para[] = method[i].getParameterTypes();
                    int temp = method[i].getModifiers();
                    System.out.print(Modifier.toString(temp) + " ");
                    System.out.print(returnType.getName() + "  ");
                    System.out.print(method[i].getName() + " ");
                    System.out.print("(");
                    for (int j = 0; j < para.length; ++j) {
                        System.out.print(para[j].getName() + " " + "arg" + j);
                        if (j < para.length - 1) {
                            System.out.print(",");
                        }
                    }
                    Class<?> exce[] = method[i].getExceptionTypes();
                    if (exce.length > 0) {
                        System.out.print(") throws ");
                        for (int k = 0; k < exce.length; ++k) {
                            System.out.print(exce[k].getName() + " ");
                            if (k < exce.length - 1) {
                                System.out.print(",");
                            }
                        }
                    } else {
                        System.out.print(")");
                    }
                    System.out.println();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if (position == 8) {

            try {
                Class<?> clazz = Class.forName("com.larry.coursesamples.ref.Nexus");
                Object instance = clazz.newInstance();
                Method method = clazz.getMethod("setName", String.class);
                method.invoke(instance, "Nexus 6p");

                method = clazz.getMethod("getName");
                String name = (String) method.invoke(instance);

                System.out.println(name);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        } else if (position == 9) {
            try {
                Class<?> clazz = Class.forName("com.larry.coursesamples.ref.Phone");
                Object instance = clazz.newInstance();
                // 可以直接对 private 的属性赋值
                Field field = clazz.getDeclaredField("price");
                field.setAccessible(true);
                field.set(instance, 3188);
                System.out.println(field.get(instance));


            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        } else if (position == 10) {

            Window windon = getActivity().getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    windon.getAttributes().systemUiVisibility |=
                            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                    windon.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                            | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                    Field drawsSysBackgroundsField =
                            WindowManager.LayoutParams.class.getField("FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS");
                    windon.addFlags(drawsSysBackgroundsField.getInt(null));

                    Method setStatusBarColorMethod = Window.class.getDeclaredMethod("setStatusBarColor", int.class);
                    Method setNavigationBarColorMethod =
                            Window.class.getDeclaredMethod("setNavigationBarColor", int.class);
                    setStatusBarColorMethod.invoke(windon, Color.TRANSPARENT);
                    setNavigationBarColorMethod.invoke(windon, Color.TRANSPARENT);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                windon.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                windon.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            }
        }

    }

    public List<RefInfo> getDatas() {
        List<RefInfo> list = new ArrayList<>();

        RefInfo info = new RefInfo();
        info.setContext("forceStopPackage");
        list.add(info);

        info = new RefInfo();
        info.setContext("1通过一个对象获得完整的包名和类名");
        list.add(info);

        info = new RefInfo();
        info.setContext("2实例化Class类对象");
        list.add(info);

        info = new RefInfo();
        info.setContext("3获取一个对象的父类与实现的接口");
        list.add(info);

        info = new RefInfo();
        info.setContext("4获取某个类中的全部构造函数");
        list.add(info);

        info = new RefInfo();
        info.setContext("5通过反射机制实例化一个类的对象");
        list.add(info);

        info = new RefInfo();
        info.setContext("6获取某个类的全部属性");
        list.add(info);

        info = new RefInfo();
        info.setContext("7获取某个类的全部方法");
        list.add(info);

        info = new RefInfo();
        info.setContext("8通过反射机制调用某个类的方法");
        list.add(info);

        info = new RefInfo();
        info.setContext("9通过反射机制操作某个类的属性");
        list.add(info);

        info = new RefInfo();
        info.setContext("反射机制应用于工厂模式");
        list.add(info);

        info = new RefInfo();
        info.setContext("设置透明的状态栏和导航栏");
        list.add(info);

        info = new RefInfo();
        info.setContext("动态代理");
        list.add(info);

        return list;
    }
}
