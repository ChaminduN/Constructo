package com.example.Constructo;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.Constructo.Model.Products;
import com.example.Constructo.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProductDetailsActivity extends AppCompatActivity {

    //private FloatingActionButton addToCartBtn;
    private Button addToCartButton;
    private ImageView productImage;
    private ElegantNumberButton numberButton;
    private TextView productPrice, productDescription, productName;
    private String productID = "", state="Normal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        productID = getIntent().getStringExtra("pid");

        //addToCartBtn = (FloatingActionButton) findViewById(R.id.add_product_to_cart_btn);
        addToCartButton=(Button) findViewById(R.id.pd_add_to_cart_button);
        numberButton = (ElegantNumberButton) findViewById(R.id.number_btn);
        productImage = (ImageView) findViewById(R.id.product_image_details);
        productPrice = (TextView) findViewById(R.id.product_price_details);
        productDescription = (TextView) findViewById(R.id.product_description_details);
        productName = (TextView) findViewById(R.id.product_name_details);

        getProductDetails(productID);

        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                addingToCartList();

                if (state.equals("Order Placed") || state.equals("Order Shipped"))
                {
                    Toast.makeText(ProductDetailsActivity.this, "You can order more books , once you have completed previous tasks", Toast.LENGTH_LONG).show();
                }
                else
                {
                    addingToCartList();
                }

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        CheckOrderState();
    }

    private void addingToCartList() {

        String saveCurrentTime,saveCurrentDate;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate= currentDate.format(calForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime= currentDate.format(calForDate.getTime());

       final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");

        final HashMap<String, Object> cartMap = new HashMap<>();
        cartMap.put("pid", productID);
        cartMap.put("pname", productName.getText().toString());
        cartMap.put("price", productPrice.getText().toString());
        cartMap.put("date", saveCurrentDate);
        cartMap.put("time", saveCurrentTime);
        cartMap.put("quantity", numberButton.getNumber());
        cartMap.put("discount", "");

        cartListRef.child("User View").child(Prevalent.currentOnlineUser.getPhone())
                .child("Products").child(productID)
                .updateChildren(cartMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful())
                        {
                            cartListRef.child("Admin View").child(Prevalent.currentOnlineUser.getPhone())
                                    .child("Products").child(productID)
                                    .updateChildren(cartMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(ProductDetailsActivity.this, "Added to the cart List", Toast.LENGTH_SHORT).show();

                                                Intent intent = new Intent(ProductDetailsActivity.this, HomeActivity.class);
                                                startActivity(intent);

                                            }
                                        }
                                    });
                        }

                    }
                });


    }

    private void getProductDetails(String productID) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("Products");

        productsRef.child(productID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        Products products = dataSnapshot.getValue(Products.class);

                        productName.setText(products.getPname());
                        productPrice.setText(products.getPrice());
                        productDescription.setText(products.getDescription());
                        Picasso.get().load(products.getImage()).into(productImage);



                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });
    }

    private void CheckOrderState()
    {
        DatabaseReference orderRef;
        orderRef = FirebaseDatabase.getInstance().getReference()
                .child("Orders")
                .child(Prevalent.currentOnlineUser.getPhone());

        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    String shippingState = dataSnapshot.child("state").getValue().toString();
                    //String userName = dataSnapshot.child("name").getValue().toString();
                    if (shippingState.equals("shipped"))
                    {
                        //txtTotalAmount.setText("Dear" +userName+ "\n book is shipped ");
                        //recyclerView.setVisibility(View.GONE);

                        //txtMsg1.setVisibility(View.VISIBLE);
                        //NextProcessBtn.setVisibility(View.GONE);

                        //Toast.makeText(CartActivity.this, "You can buy more Books", Toast.LENGTH_SHORT).show();
                        state = "Order Shipped";

                    }
                    else if (shippingState.equals("not shipped"))
                    {
                        state = "Order Placed";
                        //txtTotalAmount.setText("Shipping State = Not Shipped");
                        //recyclerView.setVisibility(View.GONE);

                        //txtMsg1.setVisibility(View.VISIBLE);
                        //txtMsg1.setText("Your ordred book will be recieved as soon as possible");
                        //NextProcessBtn.setVisibility(View.GONE);

                        //Toast.makeText(CartActivity.this, "You can buy more Books", Toast.LENGTH_SHORT).show();
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

        }
    });

    }
}
