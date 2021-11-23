package com.example.masketv2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


class ShopBag
{
    public int ID;
    public String NAME;
    public String PRICE;
    public ShopBag(int id, String name, String price)
    {
        ID = id;
        NAME = name;
        PRICE = price;
    }
}



public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ArrayList<ShopBag> Bag;
     TextView total_price, txtCurrentUsername;
     Button  ClearBag;

     TextView Currency;
     String currency;

     DBHelper dbHelper;

     SQLiteDatabase database;

     String CurrentUser; //Логин авторизованного пользователя


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent  = getIntent();
        this.CurrentUser = intent.getStringExtra("User");//Получаем логин авторизованного пользователя


        Bag = new ArrayList<>();
        currency = " $";
        Currency = findViewById(R.id.lbCurrency);
        Currency.setText(currency);

        txtCurrentUsername = findViewById(R.id.txtCurrentUser);
        txtCurrentUsername.setText("User: "+CurrentUser);

        ClearBag = findViewById(R.id.btnClearBag);
        ClearBag.setOnClickListener(this::RemoveDrop);


        total_price = findViewById(R.id.totalPrice);
        total_price.setText("0");


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


                ImageButton add = new ImageButton(this);
                add.setImageResource(R.drawable.ic_baseline_add);
                add.setId(cursor.getInt(idIndex));
                add.setOnClickListener(this::AddToShopBag);
                dbOutputRow.addView(add);

                ImageButton delete = new ImageButton(this);
                delete.setImageResource(R.drawable.ic_baseline_trash);
                delete.setId(cursor.getInt(idIndex));
                delete.setOnClickListener(this::RemoveDrop);
                dbOutputRow.addView(delete);


                dbOutput.addView(dbOutputRow);


            }while (cursor.moveToNext());
        }
        cursor.close();
    }


    @Override
    public void onClick(View v) {

    }

    public void AddToShopBag(View v)
    {
        DBHelper dbHelpertemp = new DBHelper(this);
        database = dbHelpertemp.getWritableDatabase();
        Cursor cursor = database.query(DBHelper.TABLE_DROPS, null, DBHelper.KEY_ID+ " = ?", new String[]{String.valueOf(v.getId())}, null, null, null);
        cursor.moveToFirst();

        int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
        int nameIndex = cursor.getColumnIndex(DBHelper.KEY_NAME);
        int priceIndex = cursor.getColumnIndex(DBHelper.KEY_PRICE);

        int ID  = cursor.getInt(idIndex);
        String NAME = cursor.getString(nameIndex);
        String PRICE = cursor.getString(priceIndex);
        cursor.close();

        ShopBag item = new ShopBag(ID,NAME, PRICE);
        Bag.add(item);
        //total_price.setText(String.valueOf(Double.valueOf(total_price.getText().toString())+Double.valueOf(PRICE)));
        GetResultSum();
        Toast.makeText(this, "Покупка "+NAME +" за "+PRICE + currency, Toast.LENGTH_SHORT).show();


    }

    private void GetResultSum()
    {
        Double sum = 0.0;
        for(int i=0;i<Bag.size();i++)
        {
            sum += Double.valueOf(Bag.get(i).PRICE);
        }
        total_price.setText(String.valueOf(sum));
    }

    private void RemoveDrop(View v)
    {
        switch (v.getId())
        {

            case R.id.btnClearBag:
                //Полностью очищает корзину покупок
                Bag = new ArrayList<>();
                break;

            default:
                //Удаляет первое вхождение предмета
                for(int i=0; i<Bag.size();i++)
                {
                    if(Bag.get(i).ID==v.getId())
                    {
                        Toast.makeText(this, "Удаление из корзины "+ Bag.get(i).NAME, Toast.LENGTH_SHORT).show();
                        Bag.remove(i);
                        break;
                    }
                }
                break;
        }
        GetResultSum();
    }

}