package com.larry.lite.db;

/**
 * Created by Larry on 2017/5/18.
 */

public class LiteTable {
    public static final String TABLE = "plugins";

    public static final String _ID = "_id";
    public static final String PID = "pid";
    public static final String NAME = "name";
    public static final String URL = "url";
    public static final String PATH = "path";
    public static final String SIZE = "size";
    public static final String MD5 = "md5";
    public static final String DESC = "desc";
    public static final String LASTLAUNCHTIME = "lastLaunchTime";
    public static final String READY = "ready";
    public static final String STATE = "state";
    public static final String DOWNLOADED = "downloaded";

    public static final String[][] COLUMNS = {
            //
            {_ID, "INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT"},
            //
            {PID, "INTEGER NOT NULL"},
            //
            {NAME, "TEXT"},
            //
            {URL, "TEXT NOT NULL"},
            //
            {PATH, "TEXT"},
            //
            {SIZE, "TEXT"},
            //
            {MD5, "TEXT"},
            //
            {DESC, "TEXT"},
            //
            {LASTLAUNCHTIME, "INTEGER(8) DEFAULT 0"},
            //
            {READY, "TINYINT DEFAULT 0"},
            //
            {STATE, "SMALLINT DEFAULT 0"},
            //
            {DOWNLOADED, "INTEGER(8) DEFAULT 0)"}

    };



}
