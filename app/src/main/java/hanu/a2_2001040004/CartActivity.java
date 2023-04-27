package hanu.a2_2001040004;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hanu.a2_2001040004.adapter.CartItemAdapter;
import hanu.a2_2001040004.adapter.ProductAdapter;
import hanu.a2_2001040004.database.DBHelper;
import hanu.a2_2001040004.model.Product;

public class CartActivity extends AppCompatActivity implements CartItemAdapter.AfterTextChangeListener {

    private RecyclerView rcvProduct;
    private List<Product> products;
    private CartItemAdapter cartItemAdapter;
    private TextView totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart_activity);
//    TODO: set return to MainActivity button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        totalPrice = findViewById(R.id.totalPrice);
        rcvProduct = findViewById(R.id.recyclerViewCartProduct);
        rcvProduct.setLayoutManager(new GridLayoutManager(this, 1));

        products = new ArrayList<>();
        accessDBGetProducts();
    }

//    TODO: get products details from DB
    private void accessDBGetProducts() {
        DBHelper db = new DBHelper(CartActivity.this);
        products = db.getProductsFromDB();
        cartItemAdapter = new CartItemAdapter(products, this);
        rcvProduct.setAdapter(cartItemAdapter);
        db.close();
    }

//    TODO: Override method from CartItemAdapter to update total price
    @Override
    public void afterTotalPriceChange(int position) {
        int price = 0;
        for (Product product : products) {
            price += product.getTotalPrice();
        }
        totalPrice.setText(String.valueOf(price));
    }

//    TODO: Inflate the menu, add item to action bar if present
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
}
