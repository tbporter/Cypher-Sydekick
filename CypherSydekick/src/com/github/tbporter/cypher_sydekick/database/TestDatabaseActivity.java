package com.github.tbporter.cypher_sydekick.database;

import java.util.List;
import java.util.Random;

import com.github.tbporter.cypher_sydekick.R;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

public class TestDatabaseActivity extends ListActivity {
  private UserKeyDOA datasource;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.database_client);

    datasource = new UserKeyDOA(this);
    datasource.open();

    List<String> values = datasource.getAllUsers();

    // use the SimpleCursorAdapter to show the
    // elements in a ListView
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, values);
    setListAdapter(adapter);
  }

  // Will be called via the onClick attribute
  // of the buttons in main.xml
  public void onClick(View view) {
    @SuppressWarnings("unchecked")
    ArrayAdapter<UserKey> adapter = (ArrayAdapter<UserKey>) getListAdapter();
    UserKey username = null;
    switch (view.getId()) {
    case R.id.add:
      String[] namelist = new String[] { "Ben", "Travis", "Alex" };
      String[] keylist = new String[] { "FA8400DB", "FA8400DC", "FA8400DD" };
      int nextInt = new Random().nextInt(3);
      // save the new users to the database
      username = datasource.createUser(namelist[nextInt], keylist[nextInt]);
      adapter.add(username);
      break;
    case R.id.delete:
      if (getListAdapter().getCount() > 0) {
    	username = (UserKey) getListAdapter().getItem(0);
        //datasource.deleteUser(username);
        adapter.remove(username);
      }
      break;
    }
    adapter.notifyDataSetChanged();
  }

  @Override
  protected void onResume() {
    datasource.open();
    super.onResume();
  }

  @Override
  protected void onPause() {
    datasource.close();
    super.onPause();
  }
}
