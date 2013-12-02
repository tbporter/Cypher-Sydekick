package com.github.tbporter.cypher_sydekick.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class UserKeyDatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "cyphersydekick.db";
	private static final int DATABASE_VERSION = 1;
	
	public static final String TABLE_KEYS = "userkeys";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_USERNAME = "username";
	public static final String COLUMN_KEY = "key";
	
	private static final String DATABASE_CREATE = "create table "
		      + TABLE_KEYS + "(" + COLUMN_ID
		      + " integer primary key autoincrement, " + COLUMN_USERNAME
		      + " text not null, " + COLUMN_KEY + " text not null);";
	
	
	public UserKeyDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
}