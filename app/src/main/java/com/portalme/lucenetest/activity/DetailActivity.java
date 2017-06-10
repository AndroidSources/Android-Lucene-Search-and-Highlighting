package com.portalme.lucenetest.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.portalme.lucenetest.R;

public class DetailActivity extends AppCompatActivity {
    public static final String EXTRA_TITLE = DetailActivity.class.getCanonicalName() + ".title";
    public static final String EXTRA_INFO = DetailActivity.class.getCanonicalName() + ".info";
    public static final String EXTRA_SOURCE = DetailActivity.class.getCanonicalName() + ".source";
    public static final String EXTRA_REVIEW = DetailActivity.class.getCanonicalName() + ".review";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        TextView text = (TextView) findViewById(R.id.detail_text);
        String title = intent.getStringExtra(EXTRA_TITLE);
        String info = intent.getStringExtra(EXTRA_INFO);
        String source = intent.getStringExtra(EXTRA_SOURCE);
        String review = intent.getStringExtra(EXTRA_REVIEW);

        StringBuilder builder = new StringBuilder();
        builder.append("<p><big><big>").append(title).append("</big></big></p>");
        builder.append("<p>")
                .append(info)
                .append(" ")
                .append(source)
                .append("</p>");
        builder.append("<p>").append(review).append("</p>");

        text.setText(Html.fromHtml(builder.toString()));
        text.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
