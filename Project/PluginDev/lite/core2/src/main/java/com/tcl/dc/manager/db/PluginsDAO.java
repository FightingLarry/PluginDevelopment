//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.tcl.dc.manager.db;

import android.content.Context;
import android.text.TextUtils;
import com.tcl.dc.PluginStub;
import com.tcl.dc.manager.PluginEntity;
import com.tcl.dc.utils.FileUtils;
import java.util.Iterator;
import java.util.List;

public class PluginsDAO {
    private final Context mContext;
    private PluginSQLiteHelper mHelper;

    public PluginsDAO(Context c) {
        this.mContext = c;
        this.mHelper = new PluginSQLiteHelper(c);
    }

    public Context getContext() {
        return this.mContext;
    }

    public List<PluginEntity> queryAll() {
        List<PluginEntity> list = this.mHelper.queryAll();
        if (list != null && !list.isEmpty()) {
            PluginEntity stub;
            for (Iterator var2 = list.iterator(); var2.hasNext(); stub.priority = 0) {
                stub = (PluginEntity) var2.next();
                if (stub.state == 1 && (TextUtils.isEmpty(stub.path) || !FileUtils.exists(stub.path))) {
                    stub.state = 0;
                    stub.ready = false;
                }
            }

            return list;
        } else {
            return null;
        }
    }

    public PluginStub query(int id) {
        return this.mHelper.query(id);
    }

    public void delete(PluginEntity t) {
        this.mHelper.delete(t.id);
    }

    public void saveOrUpdate(PluginEntity entity) {
        this.mHelper.saveOrUpdate(entity);
    }

    public void saveOrUpdateAll(List<PluginEntity> entities) {
        this.mHelper.saveOrUpdateAll(entities);
    }
}