package jp.ac.titech.psg.nakano.keyphrasememo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import jp.ac.titech.psg.nakano.keyphrasememo.model.Memo;

/**
 * Created by nakanomizuki on 15/07/17.
 */
public class MemoTableHelper extends SQLiteOpenHelper {

    private static final String TAG = "MemoTableHelper";
    private static final String DB_NAME = "memo.db";
    private static final String TABLE = "memo";
    private static final int DB_VERSION = 1;
    private static final String CREATE_TABLE = "create table " + TABLE + "("
            + "id integer primary key autoincrement,"
            + "title text not null,"
            + "content text,"
            + "cdate text not null,"
            + "udate text not null"
            + ");";

    public MemoTableHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer){

    }

    public long insert(String title, String content){
        Log.d(TAG, "call insert(title=" + title + ", content=" + content + ")");
        Date date = new Date();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("cdate", date.toString());
        values.put("udate", date.toString());
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d(TAG, "inserted");
        return db.insert(TABLE, null, values);
    }

    public void update(long memoId, String title, String content){
        Log.d(TAG, "call update(memoId=" + memoId + ", title=" + title + ", content=" + content + ")");
        Date uDate = new Date();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("content", content);
        values.put("udate", uDate.toString());
        SQLiteDatabase db = this.getReadableDatabase();
        db.update(TABLE, values, "id=?", new String[]{String.valueOf(memoId)});
        Log.d(TAG, "updated");
    }

    public void deleteMemo(Long id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE, "id = " + String.valueOf(id), null);
    }

    public Memo getMemo(long memoId){
        Log.d(TAG, "call getMemo(memoId=" + memoId + "");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE + " where id =" + memoId, null);
        cursor.moveToFirst();
        return getMemoFromCursor(cursor);
    }

    public List<Memo> getMemo(Set<Long> memoIds){
        Log.d(TAG, "call getMemo(memoIds)");
        List<Memo> memos = new ArrayList<Memo>();
        if(memoIds.isEmpty()) return memos;

        String target = "(";
        for(long l:memoIds){
            target += l + ",";
        }
        target = target.substring(0, target.length()-1) + ")";
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "select * from " + TABLE + " where id in " + target;
        Log.d(TAG, "getMemo:query=" + query);
        Cursor cursor = db.rawQuery(query, null);
        boolean EOF = cursor.moveToFirst();
        while (EOF){
            memos.add(getMemoFromCursor(cursor));
            EOF = cursor.moveToNext();
        }
        cursor.close();

        return memos;
    }

    public List<Memo> getAllMemo(){
        Log.d(TAG, "call getAllMemo");
        List<Memo> memos = new ArrayList<Memo>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE + ";", null);
        boolean EOF = cursor.moveToFirst();
        while(EOF){
            memos.add(getMemoFromCursor(cursor));
            EOF = cursor.moveToNext();
        }
        cursor.close();

        return  memos;
    }

    private Memo getMemoFromCursor(Cursor c){
        Long id = c.getLong(c.getColumnIndex("id"));
        String title = c.getString(c.getColumnIndex("title"));
        String content = c.getString(c.getColumnIndex("content"));

        SimpleDateFormat sdf = new SimpleDateFormat();
        Date cdate, udate;
        try {
            cdate = sdf.parse(c.getString(c.getColumnIndex("cdate")));
            udate = sdf.parse(c.getString(c.getColumnIndex("udate")));
        }catch (ParseException e){
            cdate = null;
            udate = null;
        }

        return new Memo(id, title, content, cdate, udate);
    }

}
