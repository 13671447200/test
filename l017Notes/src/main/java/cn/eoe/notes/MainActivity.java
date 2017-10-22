package cn.eoe.notes;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import cn.eoe.notes.db.NotesDB;

public class MainActivity extends ListActivity {

	private  OnClickListener btnAddNote_ClickHandler = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			 startActivityForResult(new Intent(MainActivity.this,AtyEditeNote.class), REQUEST_CODE_ADD_NOTE);	
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		db= new NotesDB(this);
		dbRead=db.getReadableDatabase();
		adapter = new SimpleCursorAdapter(this, R.layout.notes_list_cell, null,
				new String[] { NotesDB.COLUMN_NAME_NOTE_NAME, NotesDB.COLUMN_NAME_NOTE_DATE },
				new int[] { R.id.tvName, R.id.tvDate });
		setListAdapter(adapter);
		refreshNotesListView();
		findViewById(R.id.btnAddNote).setOnClickListener(btnAddNote_ClickHandler);
		
	}
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Cursor c= adapter.getCursor();
		c.moveToPosition(position);
		//读取内部的值
		Intent i=new Intent(MainActivity.this,AtyEditeNote.class);
		i.putExtra(AtyEditeNote.EXTRA_NOTE_ID, c.getInt(c.getColumnIndex(NotesDB.COLUMN_NAME_ID)));
		i.putExtra(AtyEditeNote.EXTRA_NOTE_NAME, c.getString(c.getColumnIndex(NotesDB.COLUMN_NAME_NOTE_NAME)));
		i.putExtra(AtyEditeNote.EXTRA_NOTE_CONTENT, c.getString(c.getColumnIndex(NotesDB.COLUMN_NAME_NOTE_CONTENT)));
		startActivityForResult(i, REQUEST_CODE_EDIT_NOTE);
		super.onListItemClick(l, v, position, id);
	}
	 @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch (requestCode) {
		case REQUEST_CODE_ADD_NOTE:
		case REQUEST_CODE_EDIT_NOTE:
			if(resultCode==Activity.RESULT_OK ){
				//刷新列表
				refreshNotesListView( );
				
			}
			
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	} 
	
	public  void refreshNotesListView() {
		adapter.changeCursor(dbRead.query(NotesDB.TABLE_NAME_NOTES, null, null, null, null, null, null));
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	private SimpleCursorAdapter adapter =null;
	private NotesDB db;
	private SQLiteDatabase dbRead;
	public static final int REQUEST_CODE_ADD_NOTE=1;
	public static final int REQUEST_CODE_EDIT_NOTE=2;
}
