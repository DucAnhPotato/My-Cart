package hanu.a2_2001040004.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import hanu.a2_2001040004.model.Product;

public class DBHelper extends SQLiteOpenHelper {
    public static final String CART = "cart";
    public static final String ID = "id";
    public static final String PRODUCT_ID = "productId";
    public static final String THUMBNAIL = "thumbnail";
    public static final String NAME = "name";
    public static final String CATEGORY = "category";
    public static final String UNIT_PRICE = "unitPrice";
    public static final String QUANTITY = "quantity";
    public static final String TOTAL_PRICE = "totalUnitPrice";
    public static final String DB_NAME = CART + ".db";
    public static final int DB_VERSION = 1;

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "Create table " + CART + " (" +
                ID + " integer primary key autoincrement not null," +
                PRODUCT_ID + " integer," +
                THUMBNAIL + " text," +
                NAME + " text," +
                CATEGORY + " text," +
                UNIT_PRICE + " integer," +
                QUANTITY + " integer," +
                TOTAL_PRICE + " integer" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String sql = "drop table if exists " + CART;
        db.execSQL(sql);

        onCreate(db);
    }

//    TODO: close db
    public boolean addProductToCart(Product product) {
        if (productIsInCart(product)) {
            return increaseProductQuantity(product);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        try {
            cv.put(PRODUCT_ID, product.getId());
            cv.put(THUMBNAIL, product.getThumbnail());
            cv.put(NAME, product.getName());
            cv.put(CATEGORY, product.getCategory());
            cv.put(UNIT_PRICE, product.getUnitPrice());
            cv.put(QUANTITY, product.getQuantity());
            cv.put(TOTAL_PRICE, product.getQuantity()*product.getUnitPrice());
            long result = db.insert(CART, null, cv);
            return result > 0;
        } finally {
            db.close();
        }
    }

//    TODO: close db, cursor
    public boolean productIsInCart(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "select * from cart " +
                "where productId = " + product.getId();

        Cursor cursor = db.rawQuery(sql, null);
        try {
            if (cursor.moveToFirst()) {
                return true;
            } else {
                return false;
            }
        } finally {
            cursor.close();
            db.close();
        }
    }

//    TODO: close db
    public boolean increaseProductQuantity(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        int previousQuantity = getProductQuantity(product);
        String productId = PRODUCT_ID + " = " + product.getId();

        try {
            cv.put(QUANTITY, previousQuantity+1);
            long result = db.update(CART, cv, productId, null);
            changeTotalPrice(product);
            return result > 0;
        } finally {
            db.close();
        }
    }

//    TODO: close db
    public boolean decreaseOrRemoveProductQuantity(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        int previousQuantity = getProductQuantity(product);
        String productId = PRODUCT_ID + " = " + product.getId();

        try {
            cv.put(QUANTITY, previousQuantity-1);
            long result = db.update(CART, cv, productId, null);
            changeTotalPrice(product);
            int newQuantity = getProductQuantity(product);
            if (newQuantity == 0) removeProduct(product);

            return result > 0;
        } finally {
            db.close();
        }
    }

//    TODO: not close cursor, db
    private int getProductQuantity(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "select quantity from cart " +
                "where productId = " + product.getId();

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            int columnQuantity = cursor.getColumnIndex(QUANTITY);
            int quantity =  cursor.getInt(columnQuantity);
            return quantity;
        }
        cursor.close();
        db.close();
        return -1;
    }

//    TODO: not close db
    private void changeTotalPrice(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        String productId = PRODUCT_ID + " = " + product.getId();

        cv.put(TOTAL_PRICE, getProductQuantity(product)*product.getUnitPrice());
        db.update(CART, cv, productId, null);
    }

//    TODO: not close db
    public void removeProduct(Product product) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(CART,PRODUCT_ID + "=?",new String[]{String.valueOf(product.getId())});
    }

//    TODO: close cursor, db
    public List<Product> getProductsFromDB() {
        List<Product> productsFromDB = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "select * from " + CART;
        Cursor cursor = db.rawQuery(sql, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    String cartItemName, cartItemThumbnail;
                    int cartItemPrice, cartItemQuantity, cartItemProductId, cartItemTotalPrice;

                    int columnProductId = cursor.getColumnIndex(PRODUCT_ID);
                    int columnThumbnail = cursor.getColumnIndex(THUMBNAIL);
                    int columnName = cursor.getColumnIndex(NAME);
                    int columnPrice = cursor.getColumnIndex(UNIT_PRICE);
                    int columnQuantity = cursor.getColumnIndex(QUANTITY);
                    int columnTotalPrice = cursor.getColumnIndex(TOTAL_PRICE);

                    cartItemProductId = cursor.getInt(columnProductId);
                    cartItemThumbnail = cursor.getString(columnThumbnail);
                    cartItemName = cursor.getString(columnName);
                    cartItemPrice = cursor.getInt(columnPrice);
                    cartItemQuantity = cursor.getInt(columnQuantity);
                    cartItemTotalPrice = cursor.getInt(columnTotalPrice);

                    productsFromDB.add(new Product(cartItemProductId, cartItemThumbnail, cartItemName, cartItemPrice, cartItemQuantity, cartItemTotalPrice));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }

        return productsFromDB;
    }
}
