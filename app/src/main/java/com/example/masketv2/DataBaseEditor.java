package com.example.masketv2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

public class DataBaseEditor extends AppCompatActivity implements View.OnClickListener {

    Button Add, GoToMarket;
    EditText Name, Price;

    String currency;

    DBHelper dbHelper;

    SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_editor);




        currency = " $";

        Name = findViewById(R.id.txtName);

        Price = findViewById(R.id.txtPrice);

        Add = findViewById(R.id.btnAdd);
        Add.setOnClickListener(this::onClick);

        GoToMarket = findViewById(R.id.btnGoToMarket);
        GoToMarket.setOnClickListener(this::onClick);


        dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();


        CreateDropListView();
    }


    public void CreateDropListView(){
        Cursor cursor = database.query(DBHelper.TABLE_DROPS, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int nameIndex = cursor.getColumnIndex(DBHelper.KEY_NAME);
            int priceIndex = cursor.getColumnIndex(DBHelper.KEY_PRICE);
            TableLayout dbOutput = findViewById(R.id.dbtable);
            dbOutput.removeAllViews();

            do {
                TableRow dbOutputRow = new TableRow(this);
                dbOutputRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

                TextView id = new TextView(this);
                params.weight =1.0f;
                id.setLayoutParams(params);
                id.setText(cursor.getString(idIndex));
                dbOutputRow.addView(id);

                TextView drop_name = new TextView(this);
                params.weight =4.0f;
                drop_name.setLayoutParams(params);
                drop_name.setText(cursor.getString(nameIndex));
                dbOutputRow.addView(drop_name);

                TextView price = new TextView(this);
                params.weight =2.0f;
                price.setLayoutParams(params);
                price.setText(cursor.getString(priceIndex)+currency);
                dbOutputRow.addView(price);


                ImageButton delete = new ImageButton(this);
                delete.setImageResource(R.drawable.ic_baseline_trash);
                delete.setId(cursor.getInt(idIndex));
                delete.setOnClickListener(this::onClick);
                dbOutputRow.addView(delete);


                dbOutput.addView(dbOutputRow);


            }while (cursor.moveToNext());
        }
        cursor.close();
    }







    @Override
    public void onClick(View v) {
        database = dbHelper.getWritableDatabase();
        switch (v.getId())
        {
            case R.id.btnAdd:
                String name = Name.getText().toString();
                String price = Price.getText().toString();

                ContentValues contentValues = new ContentValues();
                contentValues.put(DBHelper.KEY_NAME, name);
                contentValues.put(DBHelper.KEY_PRICE, price);

                database.insert(DBHelper.TABLE_DROPS, null, contentValues);
                Name.setText("");
                Price.setText("");
                CreateDropListView();
                break;

            case R.id.btnGoToMarket:
                Intent MarketPage = new Intent(this,MainActivity.class);
                startActivity(MarketPage);
                break;

            default:
                database.delete(DBHelper.TABLE_DROPS,DBHelper.KEY_ID +" = ?", new String[]{String.valueOf(v.getId())});
                CreateDropListView();
                Cursor cursor = database.query(DBHelper.TABLE_DROPS,null,null,null,null,null,null);
                if(cursor.moveToFirst())
                {
                    int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
                    int nameIndex = cursor.getColumnIndex(DBHelper.KEY_NAME);
                    int priceIndex = cursor.getColumnIndex(DBHelper.KEY_PRICE);
                    int realID =1;
                    do {
                        if(realID<cursor.getInt(idIndex))
                        {
                            ContentValues content = new ContentValues();
                            content.put(DBHelper.KEY_ID, realID);
                            content.put(DBHelper.KEY_NAME, cursor.getString(nameIndex));
                            content.put(DBHelper.KEY_PRICE, cursor.getString(priceIndex));
                            database.replace(DBHelper.TABLE_DROPS,null,content);
                        }
                        realID++;
                    }while (cursor.moveToNext());
                    if(cursor.moveToLast()&&realID==cursor.getInt(idIndex))
                    {
                        database.delete(DBHelper.TABLE_DROPS, DBHelper.KEY_ID + " = ?", new String[]{cursor.getString(idIndex)});
                    }
                    cursor.close();

                }
                CreateDropListView();
                break;
        }
        database.close();
    }
}