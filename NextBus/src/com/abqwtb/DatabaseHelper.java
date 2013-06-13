package com.abqwtb;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper{

	//The Android's default system path of your application database.
	private static String DB_PATH;

	private SQLiteDatabase myDataBase; 

	private final Context myContext;

	/**
	 * Constructor
	 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
	 * @param context
	 */
	public DatabaseHelper(Context context) {

		super(context, "main", null, 1);
		this.myContext = context;
		DB_PATH = myContext.getDatabasePath("stops").getPath();
	}

	/**
	 * Copies your database from your local assets-folder to the just created empty database in the
	 * system folder, from where it can be accessed and handled.
	 * This is done by transfering bytestream.
	 * @throws IOException 
	 * */
	private void copyDataBase() throws IOException{
		SQLiteDatabase db = this.getReadableDatabase();
		db.close();
		//Open your local db as the input stream
		InputStream myInput;
		myInput = myContext.getAssets().open("stops");

		//Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(DB_PATH);

		//transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer))>0){
			myOutput.write(buffer, 0, length);
		}

		//Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
		myDataBase = SQLiteDatabase.openDatabase(DB_PATH,null, 0);
	}

	public void openDataBase() throws SQLException, IOException{

		try{
			myDataBase = SQLiteDatabase.openDatabase(DB_PATH,null, 0);
		}catch(Exception e){
			//e.printStackTrace();
			Log.v("databse","Copying Database");
			copyDataBase();
		}

	}

	@Override
	public synchronized void close() {

		if(myDataBase != null)
			myDataBase.close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public SQLiteDatabase getDatabase(){
		return myDataBase;
	}

}
