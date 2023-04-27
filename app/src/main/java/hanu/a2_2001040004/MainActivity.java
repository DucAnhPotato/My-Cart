package hanu.a2_2001040004;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import hanu.a2_2001040004.adapter.ProductAdapter;
import hanu.a2_2001040004.worker.Constants;
import hanu.a2_2001040004.model.Product;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rcvProduct;
    private List<Product> products;
    private EditText searchBar;
    private ProductAdapter productAdapter;
    private TextView resultsFoundText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBar = findViewById(R.id.searchBar);
        rcvProduct = findViewById(R.id.recyclerViewProduct);
        resultsFoundText = findViewById(R.id.resultsFoundText);

        rcvProduct.setLayoutManager(new GridLayoutManager(this, 2));

        products = new ArrayList<>();

        getProductsFromJson();
        filterSearch();
    }

//    TODO: Get list of products from the given api
    private void getProductsFromJson() {
        Handler handler = HandlerCompat.createAsync(Looper.getMainLooper());
        Constants.executorService.execute(new Runnable() {
            @Override
            public void run() {
                String json = loadJson("https://hanu-congnv.github.io/mpr-cart-api/products.json");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (json == null) {
                            Toast.makeText(MainActivity.this, "Failed to load! Try again next time.", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                JSONArray arr = new JSONArray(json);
                                for (int i = 0; i < arr.length(); i++) {
                                    JSONObject obj = arr.getJSONObject(i);

                                    int id = obj.getInt("id");
                                    String thumbnail = obj.getString("thumbnail");
                                    String name = obj.getString("name");
                                    String category = obj.getString("category");
                                    int unitPrice = obj.getInt("unitPrice");

                                    products.add(new Product(id, thumbnail, name, category, unitPrice));
                                }

                                productAdapter = new ProductAdapter(products);
                                rcvProduct.setAdapter(productAdapter);
                                changeResultsFound(products);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }

//    TODO: load the given Json link
    private String loadJson(String link) {
        URL url;
        HttpURLConnection con;
        try {
            url = new URL(link);
            con = (HttpURLConnection) url.openConnection();
            con.connect();

            InputStream is = con.getInputStream();
            StringBuilder sb = new StringBuilder();
            String line;
            Scanner sc = new Scanner(is);

            while (sc.hasNextLine()) {
                line = sc.nextLine();
                sb.append(line);
            }

            return sb.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

//    TODO: apply the filter after search text change
    private void filterSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });
    }

//    TODO: filter the changes to apply new adapter
    private void filter(String text) {
        List<Product> filterProducts = new ArrayList<>();
        for (Product product : products) {
            if (product.getName().toLowerCase().contains(text.toLowerCase())) {
                filterProducts.add(product);
            }
        }
        productAdapter = new ProductAdapter(filterProducts);
        productAdapter.notifyDataSetChanged();
        rcvProduct.setAdapter(productAdapter);
        changeResultsFound(filterProducts);
    }

//    TODO: get the results found from the products
    private void changeResultsFound(List<Product> products) {
        resultsFoundText.setText(products.size() + " results found");
    }

//    TODO: Inflate the menu, add item to action bar if present
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

//    TODO: Clicking on menu icon will send to CartActivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.cartMenuIcon:
                startActivity(new Intent(MainActivity.this, CartActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}