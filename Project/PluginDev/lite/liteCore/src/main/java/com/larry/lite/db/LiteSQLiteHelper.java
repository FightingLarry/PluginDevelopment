
package com.larry.lite.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import com.larry.lite.utils.CollectionUtils;
import com.larry.lite.utils.Streams;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class LiteSQLiteHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "t_plugins";
    private static final String TABLE_NAME = "plugins";

    public LiteSQLiteHelper(Context context) {
        super(context, DB_NAME, (CursorFactory) null, DB_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String createTableSQL =
                "CREATE TABLE [plugins] (  [_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,   [pid] INTEGER NOT NULL,   [name] TEXT,   [url] TEXT NOT NULL,   [path] TEXT,   [size] INTEGER(8) DEFAULT 0,   [md5] TEXT,   [desc] TEXT,   [lastLaunchTime] INTEGER(8) DEFAULT 0,   [ready] TINYINT DEFAULT 0,   [state] SMALLINT DEFAULT 0,   [downloaded] INTEGER(8) DEFAULT 0);";
        db.execSQL(createTableSQL);
        String createIndexSQL = "CREATE UNIQUE INDEX [index_unique_id] ON [plugins] ([pid]);";
        db.execSQL(createIndexSQL);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS plugins");
        this.onCreate(db);
    }

    private LiteEntity fromCursor(Cursor cursor) {
        LiteEntity entity = new LiteEntity();
        entity.id = cursor.getInt(cursor.getColumnIndex("pid"));
        entity.name = cursor.getString(cursor.getColumnIndex("name"));
        entity.url = cursor.getString(cursor.getColumnIndex("url"));
        entity.path = cursor.getString(cursor.getColumnIndex("path"));
        entity.size = cursor.getLong(cursor.getColumnIndex("size"));
        entity.md5 = cursor.getString(cursor.getColumnIndex("md5"));
        entity.desc = cursor.getString(cursor.getColumnIndex("desc"));
        entity.lastLaunchTime = cursor.getLong(cursor.getColumnIndex("lastLaunchTime"));
        entity.ready = cursor.getInt(cursor.getColumnIndex("ready")) != 0;
        entity.state = cursor.getInt(cursor.getColumnIndex("state"));
        entity.downloaded = cursor.getLong(cursor.getColumnIndex("downloaded"));
        return entity;
    }

    private ContentValues toValues(LiteEntity entity, boolean insert) {
        ContentValues values = new ContentValues();
        if (insert) {
            values.put("pid", Integer.valueOf(entity.id));
        }

        values.put("name", entity.name);
        values.put("url", entity.url);
        values.put("path", entity.path);
        values.put("size", Long.valueOf(entity.size));
        values.put("md5", entity.md5);
        values.put("desc", entity.desc);
        values.put("lastLaunchTime", Long.valueOf(entity.lastLaunchTime));
        values.put("ready", Integer.valueOf(entity.ready ? 1 : 0));
        values.put("state", Integer.valueOf(entity.state));
        values.put("downloaded", Long.valueOf(entity.downloaded));
        return values;
    }

    private List<Integer> all() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("plugins", new String[] {"pid"}, (String) null, (String[]) null, (String) null,
                (String) null, (String) null);
        ArrayList results = null;

        try {
            if (cursor != null && cursor.getCount() > 0) {
                results = new ArrayList(cursor.getCount());

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(0);
                    results.add(Integer.valueOf(id));
                }
            }
        } finally {
            Streams.safeClose(cursor);
        }

        return results;
    }

    public List<LiteEntity> queryAll() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("plugins", (String[]) null, (String) null, (String[]) null, (String) null,
                (String) null, (String) null);
        ArrayList results = null;

        try {
            if (cursor != null && cursor.getCount() > 0) {
                results = new ArrayList(cursor.getCount());

                while (cursor.moveToNext()) {
                    LiteEntity entity = this.fromCursor(cursor);
                    results.add(entity);
                }
            }
        } finally {
            Streams.safeClose(cursor);
        }

        return results;
    }

    public LiteEntity query(int pluginId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selections = "pid = ?";
        String[] args = new String[] {String.valueOf(pluginId)};
        Cursor cursor =
                db.query("plugins", (String[]) null, selections, args, (String) null, (String) null, (String) null);
        LiteEntity result = null;

        try {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext()) {
                result = this.fromCursor(cursor);
            }
        } finally {
            Streams.safeClose(cursor);
        }

        return result;
    }

    public boolean exists(int pluginId) {
        LiteEntity entity = this.query(pluginId);
        return entity != null;
    }

    public int delete(int pluginId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selections = "pid = ?";
        String[] args = new String[] {String.valueOf(pluginId)};
        return db.delete("plugins", selections, args);
    }

    public int saveOrUpdate(LiteEntity entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean exists = this.exists(entity.id);
        if (exists) {
            String selections = "pid = ?";
            String[] args = new String[] {String.valueOf(entity.id)};
            ContentValues values = this.toValues(entity, false);
            return db.update("plugins", values, selections, args);
        } else {
            ContentValues values = this.toValues(entity, true);
            long id = db.insert("plugins", (String) null, values);
            return id > 0L ? 1 : 0;
        }
    }

    public int saveOrUpdateAll(List<LiteEntity> entities) {
        if (entities != null && !entities.isEmpty()) {
            List<Integer> ids = this.all();
            List<LiteEntity> updates = null;
            List<LiteEntity> inserts = null;
            if (ids != null && !ids.isEmpty()) {
                updates = new ArrayList();
                inserts = new ArrayList();
                Iterator var5 = entities.iterator();

                while (var5.hasNext()) {
                    LiteEntity entity = (LiteEntity) var5.next();
                    if (ids.contains(Integer.valueOf(entity.id))) {
                        updates.add(entity);
                    } else {
                        ((List) inserts).add(entity);
                    }
                }
            } else {
                inserts = entities;
            }

            SQLiteDatabase db = this.getWritableDatabase();
            int count = 0;

            try {
                db.beginTransaction();
                long id;
                if (!CollectionUtils.isEmpty((Collection) inserts)) {
                    for (Iterator var7 = ((List) inserts).iterator(); var7.hasNext(); count += id > 0L ? 1 : 0) {
                        LiteEntity entity = (LiteEntity) var7.next();
                        ContentValues values = this.toValues(entity, true);
                        id = db.insert("plugins", (String) null, values);
                    }
                }

                if (!CollectionUtils.isEmpty(updates)) {
                    String selections = "pid = ?";
                    String[] args = new String[1];

                    ContentValues values;
                    for (Iterator var19 = updates.iterator(); var19.hasNext(); count +=
                            db.update("plugins", values, selections, args)) {
                        LiteEntity entity = (LiteEntity) var19.next();
                        args[0] = String.valueOf(entity.id);
                        values = this.toValues(entity, false);
                    }
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            return count;
        } else {
            return 0;
        }
    }
}
