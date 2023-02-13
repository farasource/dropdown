package com.farasource.sample;

import android.graphics.Color;
import android.os.Bundle;

import com.farasource.component.dropdown.DropdownView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        DropdownView dropdownView = findViewById(R.id.dropdown);
        dropdownView.setExpanded(false); // or setExpanded(false, /* animate */true)
        dropdownView.setElevation(0);
        dropdownView.setCardCornerRadius(10);
        dropdownView.setCardBackgroundColor(getResources().getColor(R.color.purple_500)); // or setCardBackground(drawable)
        dropdownView.setCardBackgroundColorExpanded(getResources().getColor(R.color.purple_500)); // option
        dropdownView.setArrow(com.farasource.component.dropdown.R.drawable.ic_round_arrow_right_24); // or setArrow(drawable)
        dropdownView.setArrowTint(Color.WHITE);
        dropdownView.setArrowTintExpanded(Color.WHITE); // option
        dropdownView.setArrowRotation(DropdownView.QUARTER);
        dropdownView.setUseDivider(false);
        dropdownView.setDividerColor(0xffe2e2e2);
        dropdownView.setDividerHeight(1);
        dropdownView.setTitleBackgroundColorExpanded(Color.TRANSPARENT);
        dropdownView.setTitleText("Hello world!");
        dropdownView.setTitleTextColor(Color.WHITE);
        dropdownView.setTitleTextColorExpanded(Color.WHITE); // option
        dropdownView.setTitleTextSize(17);
        //dropdownView.setTitleTypeface(typeface); // or setTitleTypeface(typeface, /* bold */ true)
        dropdownView.setContentText("World world world world world..."); // or setHtmlContent(content)
        dropdownView.setContentTextColor(Color.WHITE);
        dropdownView.setContentTextSize(17);
        //dropdownView.setContentTypeface(typeface); // or setContentTypeface(typeface, /* bold */ false)
    }
}