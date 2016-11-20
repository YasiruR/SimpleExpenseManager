package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.ui.MainActivity;


public class PersistantAccountDAO implements AccountDAO {

    DatabaseHelper db = MainActivity.dbconnector;
    SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();

    @Override
    public List<String> getAccountNumbersList() {
        List<String> accNumbersList = new ArrayList<String>();
        Cursor res = sqLiteDatabase.rawQuery("SELECT acc_no FROM accounts", null);
        if(res.getCount()>0){
            while(res.moveToNext()){
                String acc_no = res.getString(0);
                accNumbersList.add(acc_no);
            }
            res.close();
            return accNumbersList;
        }else{
            System.out.println("CURSOR ERROR!!!");
            return null;
        }

    }

    @Override
    public List<Account> getAccountsList() {
        List<Account> accountList = new ArrayList<Account>();
        Cursor res = sqLiteDatabase.rawQuery("SELECT acc_no,bank_name,acc_holder_name,balance FROM accounts", null);
        if(res.getCount()>0){
            while(res.moveToNext()){
                String acc_no = res.getString(0);
                String bank_name = res.getString(1);
                String acc_holder_name = res.getString(2);
                String balance = res.getString(3);
                Account acc = new Account(acc_no,bank_name,acc_holder_name,Double.parseDouble(balance));
                accountList.add(acc);
            }
            res.close();
        }
        return accountList;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM accounts WHERE acc_no LIKE '" + accountNo + "'", null);
        if (res != null) {
            String acc_no = res.getString(0);
            String bank_name = res.getString(1);
            String acc_holder_name = res.getString(2);
            String balance = res.getString(3);
            Account c = new Account(acc_no, bank_name, acc_holder_name, Double.parseDouble(balance));
            return c;
        }else {
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }

    }

    @Override
    public void addAccount(Account account) {
        Cursor res = sqLiteDatabase.rawQuery("SELECT * FROM accounts WHERE acc_no LIKE '"+account.getAccountNo()+"'",null);
        if(res.getCount()>0){

        }else{
            ContentValues cv = new ContentValues();
            cv.put("acc_no",account.getAccountNo());
            cv.put("bank_name",account.getBankName());
            cv.put("acc_holder_name",account.getAccountHolderName());
            cv.put("balance",account.getBalance());
            if((sqLiteDatabase.insert("accounts",null,cv))==-1){
                System.out.println("ERROR");
            }
        }
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        sqLiteDatabase.delete("accounts","acc_no = ?",new String[] {accountNo});
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        Cursor res;
        String val="";
        ContentValues values;
        res = sqLiteDatabase.rawQuery("SELECT acc_no FROM accounts WHERE acc_no LIKE '"+accountNo+"'",null);
        if(res.getCount()>0){
            Cursor getbal = sqLiteDatabase.rawQuery("SELECT balance FROM accounts WHERE acc_no LIKE '"+accountNo+"'",null);
            if(getbal.moveToFirst()){
                while(!getbal.isAfterLast()){
                    val = getbal.getString(getbal.getColumnIndex("balance"));
                    getbal.moveToNext();
                }
            }
            String newBal;
        switch (expenseType) {
            case EXPENSE:
                newBal = Double.toString(Double.parseDouble(val)-amount);
                values= new ContentValues();
                values.put("balance",newBal);
                sqLiteDatabase.update("accounts",values,"acc_no = ?",new String[] {accountNo});
                break;
            case INCOME:
                newBal = Double.toString(Double.parseDouble(val)+amount);
                values = new ContentValues();
                values.put("balance",newBal);
                sqLiteDatabase.update("accounts",values,"acc_no = ?",new String[] {accountNo});
                break;
        }
        }else{
            String msg = "Account " + accountNo + " is invalid.";
            throw new InvalidAccountException(msg);
        }

    }
}
