package com.shalgachev.moscowpublictransport.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.shalgachev.moscowpublictransport.R;
import com.shalgachev.moscowpublictransport.data.TransportType;

public class AddTransportActivity extends AppCompatActivity {
    private TransportType mTransportType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transport);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mTransportType = (TransportType) getIntent().getExtras().getSerializable("transport_type");

        if (mTransportType == null)
            throw new IllegalArgumentException("Transport type is null");

        CharSequence extra;
        switch (mTransportType) {
            case BUS:
                extra = getString(R.string.add_bus_title_extra);
                break;
            case TROLLEY:
                extra = getString(R.string.add_trolley_title_extra);
                break;
            case TRAM:
                extra = getString(R.string.add_tram_title_extra);
                break;
            default:
                throw new IllegalArgumentException("Unexpected transport type");
        }

        setTitle(getString(R.string.add_transport_title, extra));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
