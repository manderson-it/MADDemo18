package com.example.chefk.maddemo18;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.AsyncTask;

import com.example.chefk.maddemo18.model.DataItem;
import com.example.chefk.maddemo18.model.IDataItemCRUDOperations;
import com.example.chefk.maddemo18.model.IDataItemCRUDOperationsAsync;
import com.example.chefk.maddemo18.model.LocalDataItemCRUDOperations;
import com.example.chefk.maddemo18.model.RemoteDataItemCRUDOperationsImpl;
import com.example.chefk.maddemo18.model.User;
import com.example.chefk.maddemo18.model.WebserviceURL;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class DataItemApplication extends Application implements IDataItemCRUDOperationsAsync {

    public static enum CRUDStatus {ONLINE, OFFLINE};

    private IDataItemCRUDOperations crudOperations;
    private IDataItemCRUDOperations remoteOperations;

    private static final WebserviceURL webserviceURLString = new WebserviceURL(); // see/change value in model WebserviceURL.java
    private CRUDStatus crudStatus;

    @Override
    public void onCreate() {
        super.onCreate();
        this.crudOperations = new LocalDataItemCRUDOperations(this);
                /*new RemoteDataItemCRUDOperationsImpl();*/
                /*new SimpleDataItemCRUDOperationsImpl()*/
        this.remoteOperations = new RemoteDataItemCRUDOperationsImpl();
    }

    public IDataItemCRUDOperationsAsync getCRUDOperations() {
        return this;
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void createItem(DataItem item, final ResultCallback<Long> onresult) {
        new AsyncTask<DataItem,Void,Long>() {

            @Override
            protected Long doInBackground(DataItem... dataItems) {
                if (getCrudStatus() == CRUDStatus.OFFLINE) {
                    return crudOperations.createItem(dataItems[0]);
                } else {
                    return remoteOperations.createItem(dataItems[0]);
                }
            }

            @Override
            protected void onPostExecute(Long id) {
                onresult.onresult(id);
            }
        }.execute(item);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void readAllItems(final ResultCallback<List<DataItem>> onresult) {
        new AsyncTask<Void,Void,List<DataItem>>() {
            @Override
            protected List<DataItem> doInBackground(Void... voids) {
                if (getCrudStatus() == CRUDStatus.OFFLINE) {
                    return crudOperations.readAllItems();
                } else {
                    return remoteOperations.readAllItems();
                }
            }

            @Override
            protected void onPostExecute(List<DataItem> dataItems) {
                onresult.onresult(dataItems);
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void readItem(long id, final ResultCallback<DataItem> onresult) {
        new AsyncTask<Long,Void,DataItem>() {

            @Override
            protected DataItem doInBackground(Long... longs) {
                if (getCrudStatus() == CRUDStatus.OFFLINE) {
                    return crudOperations.readItem(longs[0]);
                } else {
                    return remoteOperations.readItem(longs[0]);
                }
            }

            @Override
            protected void onPostExecute(DataItem dataItem) {
                onresult.onresult(dataItem);
            }
        }.execute(id);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void updateItem(final long id, final DataItem item, final ResultCallback<Boolean> onresult) {
        new AsyncTask<Void,Void,Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                if (getCrudStatus() == CRUDStatus.OFFLINE) {
                    return crudOperations.updateItem(id, item);
                } else {
                    return remoteOperations.updateItem(id, item);
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                onresult.onresult(aBoolean);
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void deleteItem(final long id, final ResultCallback<Boolean> onresult) {
        new AsyncTask<Long,Void,Boolean>() {
            @Override
            protected Boolean doInBackground(Long... longs) {
                if (getCrudStatus() == CRUDStatus.OFFLINE) {
                    return crudOperations.deleteItem(id);
                } else {
                    return remoteOperations.deleteItem(id);
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                onresult.onresult(aBoolean);
            }
        }.execute();
    }


    @SuppressLint("StaticFieldLeak")
    @Override
    public void deleteAllTodos(final ResultCallback<Boolean> onresult) {
        new AsyncTask<Void,Void,Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                if (getCrudStatus() == CRUDStatus.OFFLINE) {
                    return crudOperations.deleteAllTodos();
                } else {
                    return remoteOperations.deleteAllTodos();
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                onresult.onresult(aBoolean);
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void authenticateUser(final User user, final ResultCallback<Boolean> onresult) {
        new AsyncTask<User,Void,Boolean>() {
            @Override
            protected Boolean doInBackground(User... users) {
                if (getCrudStatus() == CRUDStatus.OFFLINE) {
                    return crudOperations.authenticateUser(user);
                } else {
                    return remoteOperations.authenticateUser(user);
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                onresult.onresult(aBoolean);
            }
        }.execute();
    }

    public CRUDStatus getCrudStatus() {
        return crudStatus;
    }

    @SuppressLint("StaticFieldLeak")
    public void initialiseCRUDOperations(final ResultCallback<CRUDStatus> oninitialised) {
        new AsyncTask<Void, Void, CRUDStatus>() { // check if webservice is reachable on Thread
            @Override
            protected CRUDStatus doInBackground(Void... voids) {
                crudStatus = CRUDStatus.OFFLINE; // assume OFFLINE until url has been checked
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(webserviceURLString.getUrl()); // see very top
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(1000);
                    connection.setReadTimeout(1000);
                    int code = connection.getResponseCode();
                    if (code == 200) {
                        crudStatus = CRUDStatus.ONLINE;
                    } else {
                        crudStatus = CRUDStatus.OFFLINE;
                    }
                    return crudStatus;
                } catch (IOException e1) {
                    e1.printStackTrace();
                    return CRUDStatus.OFFLINE;  // return safely with OFFLINE
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }

            @Override
            protected void onPostExecute(CRUDStatus s) {
                super.onPostExecute(s);
                oninitialised.onresult(s);
            }
        }.execute();
    }
}
