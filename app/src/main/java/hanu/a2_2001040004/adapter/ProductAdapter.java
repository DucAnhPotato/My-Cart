package hanu.a2_2001040004.adapter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hanu.a2_2001040004.R;
import hanu.a2_2001040004.database.DBHelper;
import hanu.a2_2001040004.image.ImgLoader;
import hanu.a2_2001040004.model.Product;
import hanu.a2_2001040004.worker.Constants;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;

    public ProductAdapter(List<Product> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        product.setQuantity(1);
        if (product == null) return;

        loadProductDetails(holder, product);
        loadAddToCartButton(holder, product);
    }

    private void loadProductDetails(@NonNull ProductViewHolder holder, Product product) {
        loadImgFromURL(holder, product);
        holder.productName.setText(product.getName());
        holder.productPrice.setText("đ" + product.getUnitPrice());
    }

    private void loadImgFromURL(@NonNull ProductViewHolder holder, Product product) {
        String link = product.getThumbnail();
        Handler handler = HandlerCompat.createAsync(Looper.getMainLooper());
        Constants.executorService.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = ImgLoader.loadImage(link);
                if (bitmap != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            holder.productThumbnail.setImageBitmap(bitmap);
                        }
                    });
                }
            }
        });
    }

    private void loadAddToCartButton(@NonNull ProductViewHolder holder, Product product) {
        holder.productAddToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBHelper db = new DBHelper(view.getContext());
                db.addProductToCart(product);
                db.close();
                Toast.makeText(view.getContext(), "added " + product.getName() + " to cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {

        private ImageView productThumbnail;
        private TextView productName, productPrice;
        private ImageButton productAddToCartButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productThumbnail = itemView.findViewById(R.id.productThumbnail);
            productAddToCartButton = itemView.findViewById(R.id.addToCartButton);
        }
    }
}
