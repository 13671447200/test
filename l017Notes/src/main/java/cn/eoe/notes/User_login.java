package cn.eoe.notes;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class User_login extends Activity {
EditText edit1,edit2;
Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         final String name="zhangsan";
        final String password="1234";
        edit1= (EditText)findViewById(R.id.edit1); 
        edit2= (EditText)findViewById(R.id.edit2);
        btn= (Button)findViewById(R.id.button1);
        btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO 自动生成的方法存根
			if(edit1.getText().toString().equals(name)&&edit2.getText().toString().equals(password)){
				Toast.makeText(User_login.this, "验证成功，欢迎进入",Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(User_login.this, "验证失败，重新输入",Toast.LENGTH_LONG).show();
			}
				
			}
		});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
