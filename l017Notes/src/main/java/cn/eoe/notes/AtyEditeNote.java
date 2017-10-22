package cn.eoe.notes;

import java.io.File;
import java.io.IOException;
import java.net.ContentHandler;
import java.security.PublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.security.auth.PrivateCredentialPermission;
import org.w3c.dom.Text;
import android.R.string;
import android.app.Activity;
import android.app.LauncherActivity.ListItem;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Path;
import android.media.Image;
import android.media.MediaRouter.SimpleCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.provider.Telephony.Mms.Addr;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterViewFlipper;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import cn.eoe.notes.R.layout;
import cn.eoe.notes.db.NotesDB;

public class AtyEditeNote extends ListActivity {

	private OnClickListener btnClickHandler=new OnClickListener() {
		Intent i;
		File f;
		@Override
		public void onClick(View v) {
			switch (v .getId()){
			case R.id.btnAddPhoto:
				 i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				 f =new File(getMediaDir(),System.currentTimeMillis()+".jpg");
				if(!f.exists()){
					try{
						f.createNewFile();
					}catch (IOException e){
					
					e.printStackTrace();
				}
				}
			   currentPath = f.getAbsolutePath();
				startActivityForResult(i, REQUEST_CODE_GET_PHOTO);
				//指定输出路经
				i.putExtra(MediaStore.ACTION_VIDEO_CAPTURE, Uri.fromFile(f));
				break;
			case R.id.btnAddVedio:
				 i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			     f =new File(getMediaDir(),System.currentTimeMillis()+".mp4");
				if(!f.exists()){
					try{
						f.createNewFile();
					}catch (IOException e){
					
					e.printStackTrace();
				}
				}
			     currentPath = f.getAbsolutePath();
			     startActivityForResult(i, REQUEST_CODE_GET_VIDEO);
				//指定输出路经
				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			break;
			case R.id.btnSave:
				saveMedia(saveNote());
				setResult(RESULT_OK);
				finish();
				break;
			case R.id.btnCancel:
				setResult(RESULT_CANCELED);
				finish();
				break;
			default:
				break;
			}
			
		}
	};
	private SQLiteDatabase dbWrite;
	
	@Override
	//BUG：屏幕翻时候重新执行该方法。
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_edit_note);
		 db=new NotesDB(this);
		dbRead= db.getReadableDatabase();
		dbWrite=db.getWritableDatabase();
		adapter=new MediaAdapter(this);
		setListAdapter(adapter);
		
		etName=(EditText) findViewById(R.id.etName);
		etContent=(EditText) findViewById(R.id.etContent);
		noteId= getIntent().getIntExtra(EXTRA_NOTE_ID, -1);
		if(noteId>-1){//修改某一条日志
			etName.setText(getIntent().getStringExtra(EXTRA_NOTE_NAME));
			etName.setText(getIntent().getStringExtra(EXTRA_NOTE_CONTENT));
			Cursor c= dbRead.query(NotesDB.TABLE_NAME_MEDIA, null, NotesDB.COLUMN_NAME_MEDIA_OWNER_NOTE_ID+"=?", new String[]{noteId+""}, null, null, null);
		while(c.moveToNext()){
			adapter.add(new MediaListCellDate(c.getString(c.getColumnIndex(NotesDB.COLUMN_NAME_MEDIA_PATH)),c.getInt(c.getColumnIndex(NotesDB.COLUMN_NAME_ID))));
		}
		adapter.notifyDataSetChanged();
		}else{ 
			//该操作为添加日志操作
		}
		findViewById(R.id.btnSave).setOnClickListener(btnClickHandler);
		findViewById(R.id.btnCancel).setOnClickListener(btnClickHandler);
		findViewById(R.id.btnAddPhoto).setOnClickListener(btnClickHandler);
		findViewById(R.id.btnAddVedio).setOnClickListener(btnClickHandler);
	}
	//点击显示一张图片的方法
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		MediaListCellDate date=adapter.getItem(position);
		Intent i;
		switch (date.type) {
		case Mediatype.PHOTO:
			i=new Intent(this , AtyVideoViewer.class);
			i.putExtra(AtyPhotoViewer.EXTRA_PATH, date.path);
			startActivity(i);
			break;
		case Mediatype.VIDEO: 
			i=new Intent(this , AtyPhotoViewer.class);
			i.putExtra(AtyPhotoViewer.EXTRA_PATH, date.path);
			startActivity(i);
		default:
			break;
		}
		
		super.onListItemClick(l, v, position, id);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_GET_PHOTO:
		case REQUEST_CODE_GET_VIDEO:
			if(resultCode==RESULT_OK){
				adapter.add(new MediaListCellDate(currentPath));
				adapter.notifyDataSetChanged();			
			}
			break;

		default:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public File getMediaDir(){
		File dir=new File(Environment.getExternalStorageDirectory(), "NoteMedia");
		if(!dir.exists()){
			dir.mkdir();
		}
		return dir;
	}
	//保存书签
	public void saveMedia(int noteId){
		MediaListCellDate date;
		ContentValues cv;
		for (int i= 0 ;i< adapter.getCount();i++){
			date=adapter.getItem(i); 
			if(date.id<=-1){
				cv=new ContentValues();
				cv.put(NotesDB.COLUMN_NAME_MEDIA_PATH , date.path);
				cv.put(NotesDB.COLUMN_NAME_MEDIA_OWNER_NOTE_ID, noteId);
				dbWrite.insert(NotesDB.TABLE_NAME_MEDIA, null, cv);
			
				
			}
			
		}
		
	}
	 public int saveNote(){
      ContentValues cv= new ContentValues();
      cv.put(NotesDB.COLUMN_NAME_NOTE_NAME, etName.getText().toString());
      cv.put(NotesDB.COLUMN_NAME_NOTE_CONTENT,etContent.getText().toString());
      cv.put(NotesDB.COLUMN_NAME_NOTE_DATE,SimpleDateFormat("yyy-mm-dd hh:mm:ss").format(new Date()));
      
     if(noteId>-1){
               dbWrite.update(NotesDB.TABLE_NAME_NOTES, cv, NotesDB.COLUMN_NAME_ID+"=?", new String[]{noteId+""});
               return noteId;   	 
     }else{
    	    return (int) dbWrite.insert(NotesDB.TABLE_NAME_NOTES, null, cv); 
     }
	}
	
	 private DateFormat SimpleDateFormat(String string) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	protected void onDestroy() {
		dbRead.close();
		dbWrite.close();
		super.onDestroy();
	}

	//如果为-1则该操作为添加操作
	
	
	private int noteId=-1;
	private EditText etName,etContent;
	private MediaAdapter adapter;
	private NotesDB db;
	private SQLiteDatabase  dbRead;
	private String currentPath=null;
 
	
	//定义输出路线，方便访问
	//private  String currentPath=null;
	//方便外界进行掉用
	public static final int REQUEST_CODE_GET_PHOTO=1;
	public static final int REQUEST_CODE_GET_VIDEO=2;
	public static final String EXTRA_NOTE_ID="noteId";
	public static final String EXTRA_NOTE_NAME="noteName";
	public static final String EXTRA_NOTE_CONTENT="noteContent";
	//定义media列表  
	static class MediaAdapter extends BaseAdapter{
		public MediaAdapter(Context context){
			this.context=context;
		}
		public void add(MediaListCellDate date){
			list.add(date);
			
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public MediaListCellDate getItem(int position) {
			// TODO Auto-generated method stub
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null){
	 convertView=LayoutInflater.from(context).inflate(R.layout.media_list_cell, null);
			}{
				MediaListCellDate date= getItem(position);
				ImageView ivIcon=(ImageView) convertView.findViewById(R.id.IvIcon);
				TextView tvPath=(TextView) convertView.findViewById(R.id.tvPath);
				ivIcon.setImageResource(date.IconId);
				tvPath.setText(date.path);
			}
			return convertView;
		}
		private Context context;
		//内部存放的数据
	private List<MediaListCellDate> list=new ArrayList<AtyEditeNote.MediaListCellDate>() ;
	}
	static class MediaListCellDate{
		public int IconId;
		public MediaListCellDate(String path){
			this.path=path;
			type=Mediatype.PHOTO;
			if(path.endsWith(".jpg")){
				iconId = R.drawable.icon_photo;
			}else if(path.endsWith(".mp4")){
				iconId=R.drawable.icon_video;
				type=Mediatype.VIDEO;
			}
		}
		
	public MediaListCellDate(String path ,int id){
		this(path);
		this.id=id;
	}
		int type =0; 
		int id=-1;
		String path="";
	    int iconId=R.drawable.ic_launcher;
	}
	static class Mediatype{
		static final int PHOTO =1;
		static final int VIDEO =2;	}
	}
