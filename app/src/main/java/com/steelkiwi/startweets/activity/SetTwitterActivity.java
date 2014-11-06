package com.steelkiwi.startweets.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.steelkiwi.startweets.R;

/**
 * Created by lester on 07.11.14.
 */
public class SetTwitterActivity extends Activity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.twitter_changer);
        prefs = this.getSharedPreferences(
                "com.steelkiwi.startweets", Context.MODE_PRIVATE);
        Button setNameBtn = (Button) findViewById(R.id.btn_set_name);
        final EditText nameEditText = (EditText) findViewById(R.id.edit_text_name);
        nameEditText.setText(getTwitterName());
        final Context context = this;
        setNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEmpty(nameEditText)) {
                    Toast.makeText(context, R.string.empty_fields, Toast.LENGTH_SHORT).show();
                } else {
                    String name = nameEditText.getText().toString();
                    setTwitterName(name);
                    nameEditText.setText("");
                    finish();
                }
            }
        });
    }

    private boolean isEmpty(EditText myeditText) {
        return myeditText.getText().toString().trim().length() == 0;
    }

    private String getTwitterName() {
        return prefs.getString(MainActivity.TWITTER_SH_PREF_KEY, "ladygaga");
    }

    private void setTwitterName(String name) {
        prefs.edit().putString(MainActivity.TWITTER_SH_PREF_KEY, name).apply();
    }
}
