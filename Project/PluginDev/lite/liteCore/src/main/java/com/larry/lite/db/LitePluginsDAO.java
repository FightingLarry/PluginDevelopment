
package com.larry.lite.db;

import android.content.Context;
import android.text.TextUtils;
import com.larry.lite.LiteStub;
import com.larry.lite.utils.FileUtils;
import java.util.Iterator;
import java.util.List;

public class LitePluginsDAO {
    private final Context mContext;
    private LiteSQLiteHelper mHelper;

    public LitePluginsDAO(Context c) {
        this.mContext = c;
        this.mHelper = new LiteSQLiteHelper(c);
    }

    public Context getContext() {
        return this.mContext;
    }

    public List<LiteEntity> queryAll() {
        List<LiteEntity> list = this.mHelper.queryAll();
        if (list != null && !list.isEmpty()) {
            LiteEntity stub;
            for (Iterator var2 = list.iterator(); var2.hasNext(); stub.priority = 0) {
                stub = (LiteEntity) var2.next();
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

    public LiteStub query(int id) {
        return this.mHelper.query(id);
    }

    public void delete(LiteEntity t) {
        this.mHelper.delete(t.id);
    }

    public void saveOrUpdate(LiteEntity entity) {
        this.mHelper.saveOrUpdate(entity);
    }

    public void saveOrUpdateAll(List<LiteEntity> entities) {
        this.mHelper.saveOrUpdateAll(entities);
    }
}
