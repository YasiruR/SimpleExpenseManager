package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.ui.MainActivity;


public class PersistantTransactionsDAO implements TransactionDAO {

    DatabaseHelper db = MainActivity.dbconnector;
    SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();

    List<Transaction> transList;

    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount){
        Cursor res = sqLiteDatabase.rawQuery("SELECT acc_no FROM accounts WHERE acc_no LIKE '"+accountNo+"'",null);
        if(res.getCount()>0){
            ContentValues cv = new ContentValues();
            cv.put("date", date.toString());
            cv.put("acc_no",accountNo);
            cv.put("type",expenseType.toString());
            cv.put("amount",amount);
            if((sqLiteDatabase.insert("transactions",null,cv))==-1){
                System.out.println("ERROR INSERTING DATA !!!");
            }
        }
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {

        transList = new ArrayList<Transaction>();

        ExpenseType expenseType = null;
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM transactions",null);
        if(res.getCount()>0){
                while(res.moveToNext()){
                    String date = res.getString(res.getColumnIndex("date"));
                    String acc_no = res.getString(res.getColumnIndex("acc_no"));
                    String type = res.getString(res.getColumnIndex("type"));
                    String amount = res.getString(res.getColumnIndex("amount"));
                    if(type.equalsIgnoreCase("EXPENSE")){
                        expenseType = ExpenseType.EXPENSE;
                    }else{
                        expenseType = ExpenseType.INCOME;
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyy");
                    Date d = null;
                    try {
                        d = sdf.parse(date);
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                    Transaction t = new Transaction(d,acc_no,expenseType,Double.parseDouble(amount));
                    transList.add(t);
                }

        }else{

        }

        return transList;
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        getAllTransactionLogs();

        int size = transList.size();
        if (size <= limit) {
            return transList;
        }

        return transList.subList(size - limit, size);
    }
}
