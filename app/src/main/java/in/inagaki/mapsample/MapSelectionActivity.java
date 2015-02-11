package in.inagaki.mapsample;

import android.content.Intent;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;


public class MapSelectionActivity extends ActionBarActivity {

    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_selection);
        File file = new File(Environment.getExternalStorageDirectory(), Constants.DATA_DIRECTORY);
        File[] files = file.listFiles();
        LinkedHashMap<String, File> fileMap = new LinkedHashMap<>();
        for (File f : files) {
            fileMap.put(f.getName(), f);
        }

        Iterator<String> iterator = fileMap.keySet().iterator();
        ArrayList<String> names = new ArrayList<>(); // 表示する項目のリスト
        while(iterator.hasNext()) {
            names.add(iterator.next());
        }
        int checkedPosition = 0; // 最初に選択表示する位置(0, 1, ..)

        list = (ListView) findViewById(R.id.mapList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_single_choice, // ラジオボタンがついたリスト
                names);
        list.setAdapter(adapter);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE); // 単一項目を選択させる
        if (checkedPosition >= 0) {
            // デフォルトで選択しておく項目
            list.setItemChecked(checkedPosition, true);
        }
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view,
                                    final int position, final long id) {
                // 項目が選択されたときの処理
                // ラジオボタンのチェックは自動的に入る
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            String str = (String) list.getItemAtPosition(list.getCheckedItemPosition());
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(Constants.MAP, str).commit();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
