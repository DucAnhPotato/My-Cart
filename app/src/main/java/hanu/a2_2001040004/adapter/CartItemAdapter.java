package hanu.a2_2001040004.adapter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.os.HandlerCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hanu.a2_2001040004.R;
import hanu.a2_2001040004.database.DBHelper;
import hanu.a2_2001040004.image.ImgLoader;
import hanu.a2_2001040004.model.Product;
import hanu.a2_2001040004.worker.Constants;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {

    private List<Product> products;
    private AfterTextChangeListener mAfterTextChangeListener;

    public CartItemAdapter(List<Product> products, AfterTextChangeListener mAfterTextChangeListener) {
        this.products = products;
        this.mAfterTextChangeListener = mAfterTextChangeListener;
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item_product, parent, false);
        return new CartItemViewHolder(view, mAfterTextChangeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        Product product = products.get(position);
        if (product == null) return;

        loadCartItemDetails(holder, product);
        loadAddQuantityButton(holder, product);
        loadDecreaseQuantityButton(holder, product);
    }

    private void loadCartItemDetails(@NonNull CartItemViewHolder holder, Product product) {
        loadImgFromURL(holder, product);
        holder.cartItemName.setText(product.getName());
        holder.cartItemPrice.setText("đ" + product.getUnitPrice());
        holder.cartItemQuantity.setText(String.valueOf(product.getQuantity()));
        product.setTotalPrice();
        holder.cartItemPriceWithQuantity.setText(String.valueOf(product.getTotalPrice()));
    }

    private void loadImgFromURL(@NonNull CartItemViewHolder holder, Product product) {
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
                            holder.cartItemThumbnail.setImageBitmap(bitmap);
                        }
                    });
                }
            }
        });
    }

    private void loadAddQuantityButton(@NonNull CartItemViewHolder holder, Product product) {
        holder.addQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBHelper db = new DBHelper(view.getContext());
                db.increaseProductQuantity(product);

                product.setQuantity(product.getQuantity()+1);
                int newQuantity = product.getQuantity();
                holder.cartItemQuantity.setText(String.valueOf(newQuantity));

                product.setTotalPrice();
                holder.cartItemPriceWithQuantity.setText(String.valueOf(product.getTotalPrice()));

                db.close();
            }
        });
    }

    private void loadDecreaseQuantityButton(@NonNull CartItemViewHolder holder, Product product) {
        holder.decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBHelper db = new DBHelper(view.getContext());
                db.decreaseOrRemoveProductQuantity(product);

                product.setQuantity(product.getQuantity()-1);
                int newQuantity = product.getQuantity();
                removeCartItem(product);
                holder.cartItemQuantity.setText(String.valueOf(newQuantity));

                product.setTotalPrice();
                holder.cartItemPriceWithQuantity.setText(String.valueOf(product.getTotalPrice()));

                db.close();
            }
        });
    }

    private void removeCartItem(Product product) {
        if (product.getQuantity() == 0) {
            products.remove(product);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class CartItemViewHolder extends RecyclerView.ViewHolder implements TextWatcher {

        private ImageView cartItemThumbnail;
        private TextView cartItemName, cartItemPrice, cartItemQuantity, cartItemPriceWithQuantity;
        private ImageButton addQuantityButton, decreaseQuantityButton;
        private AfterTextChangeListener mAfterTextChangeListener;

        public CartItemViewHolder(@NonNull View itemView, AfterTextChangeListener mAfterTextChangeListener) {
            super(itemView);

            cartItemPriceWithQuantity = itemView.findViewById(R.id.cartItemPriceWithQuantity);
            cartItemThumbnail = itemView.findViewById(R.id.cartItemThumbnail);
            cartItemName = itemView.findViewById(R.id.cartItemName);
            cartItemPrice = itemView.findViewById(R.id.cartItemPrice);
            cartItemQuantity = itemView.findViewById(R.id.cartItemQuantity);
            addQuantityButton = itemView.findViewById(R.id.addQuantityButton);
            decreaseQuantityButton = itemView.findViewById(R.id.decreaseQuantityButton);

            this.mAfterTextChangeListener = mAfterTextChangeListener;
            cartItemPriceWithQuantity.addTextChangedListener(this);

        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            mAfterTextChangeListener.afterTotalPriceChange(getAdapterPosition());
        }
    }

    public interface AfterTextChangeListener {
        void afterTotalPriceChange(int position);
    }
}
