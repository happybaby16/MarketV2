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
     TextView total_price;
     Button Add;
     EditText Name, Price;

     TextView Currency;
     String currency;

     DBHelper dbHelper;

     SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bag = new ArrayList<>();
        currency = " $";
        Currency = findViewById(R.id.lbCurrency);
        Currency.setText(currency);

        Name = findViewById(R.id.txtName);

        Price = findViewById(R.id.txtPrice);

        Add = findViewById(R.id.btnAdd);
        Add.setOnClickListener(this::onClick);


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
            default:
                RemoveDrop(v);//Удаление предметов из корзины покупок и пересчёт итоговой суммы корзины




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
                CreateDropListView();
                break;
        }
        database.close();
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

        //Пересчёт итоговой суммы с учётом удаления предмета
        ArrayList<Integer> indexDel = new ArrayList<Integer>();
        for(int i=0;i<Bag.size();i++)
        {
            //Ищем индексы для удаления
            if(Bag.get(i).ID==v.getId())
            {
                indexDel.add(i);
            }
        }
        //Удаляем элементы, индексы которых находятся в indexDel
        for(int i=indexDel.size()-1;i>-1;i--)
        {
            int index = indexDel.get(i);
            Toast.makeText(this, "Удаление из базы и из корзины "+ Bag.get(index).NAME, Toast.LENGTH_SHORT).show();
            Bag.remove(index);
        }

        GetResultSum();
    }

}