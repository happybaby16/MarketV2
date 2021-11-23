package com.example.masketv2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button logIn, singIn;
    TextView login, password;

    DBHelperAuth dbHelperAuth;
    SQLiteDatabase database;

    Integer access;
    String CurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.username);
        password = findViewById(R.id.password);
        logIn = findViewById(R.id.logIn);
        singIn = findViewById(R.id.singIn);
        logIn.setOnClickListener(this);
        singIn.setOnClickListener(this);
        access = 1;//Уровень доступа клиента

        dbHelperAuth = new DBHelperAuth(this);
        database = dbHelperAuth.getWritableDatabase();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.logIn:
                Cursor cursor = database.query(dbHelperAuth.TABLE_NAME,null,null,null, null, null,null);
                if (cursor.moveToFirst()) {
                    int idIndex = cursor.getColumnIndex(dbHelperAuth.KEY_ID);
                    int usernameIndex = cursor.getColumnIndex(dbHelperAuth.KEY_USERNAME);
                    int passwordIndex = cursor.getColumnIndex(dbHelperAuth.KEY_PASSWORD);
                    int accessIndex = cursor.getColumnIndex(dbHelperAuth.KEY_ACCESS);
                    do
                    {
                        String usernameDB = cursor.getString(usernameIndex);
                        String passwordDB = cursor.getString(passwordIndex);
                        if(usernameDB.equals(login.getText().toString())&&passwordDB.equals(password.getText().toString())) {
                            access = Integer.valueOf(cursor.getString(accessIndex));
                            CurrentUser = usernameDB;
                            break;//выход из цикла, чтобы не проверять лишние записи
                        }
                    }while (cursor.moveToNext());
                }
                cursor.close();
                if(CurrentUser!=null) {
                    if (access == 0)//Админ
                    {
                        Intent DBEditorPage = new Intent(this, DataBaseEditor.class);
                        DBEditorPage.putExtra("User", CurrentUser);
                        startActivity(DBEditorPage);
                    } else if (access == 1)//Клиент
                    {
                        Intent MarketPage = new Intent(this, MainActivity.class);
                        MarketPage.putExtra("User", CurrentUser);
                        startActivity(MarketPage);
                    }
                    Toast.makeText(this, "Добро пожаловать, "+CurrentUser, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(this, "Неверно введен логин или пароль!", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.singIn:
                boolean doSingIn = true;

                Cursor cursorFinder = database.query(dbHelperAuth.TABLE_NAME,null,null,null, null, null,null);
                if (cursorFinder.moveToFirst()) {
                    int usernameIndex = cursorFinder.getColumnIndex(dbHelperAuth.KEY_USERNAME);
                    do
                    {
                        String usernameDB = cursorFinder.getString(usernameIndex);
                        if(usernameDB.equals(login.getText().toString())) {
                            doSingIn = false;//Запрещаем регистрацию по данному логину, поскольку он есть в БД
                            break;//выход из цикла, чтобы не проверять лишние записи
                        }
                    }while (cursorFinder.moveToNext());
                }
                cursorFinder.close();


                if(doSingIn) {
                    String Username = login.getText().toString();
                    String Password = password.getText().toString();
                    String Access = String.valueOf(access);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(dbHelperAuth.KEY_USERNAME, Username);
                    contentValues.put(dbHelperAuth.KEY_PASSWORD, Password);
                    contentValues.put(dbHelperAuth.KEY_ACCESS, Access);

                    database.insert(dbHelperAuth.TABLE_NAME, null, contentValues);
                    login.setText("");
                    password.setText("");
                    Toast.makeText(this, "Вы успешно зарегистрировались по логином: "+Username, Toast.LENGTH_SHORT).show();
                    break;
                }
                else
                {
                    Toast.makeText(this, "Данный логин уже зарегистрирован, попробуйте дрогой логин для входа. ", Toast.LENGTH_SHORT).show();
                }

        }
    }

}