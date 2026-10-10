
package com.example.cellidreader;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.telephony.CellInfo;
import android.telephony.CellIdentity;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoWcdma;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoTdscdma;
import android.telephony.TelephonyManager;
import android.telephony.CellSignalStrength;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.Typeface;

import java.util.List;

public class MainActivity extends Activity {

    private static final int PERMISSION_REQUEST = 100;
    private TextView output;
    private TelephonyManager telephony;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView title = new TextView(this);
        title.setText("CELL ID READER");
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        layout.addView(title);

        Button refresh = new Button(this);
        refresh.setText("READ CELL INFORMATION");
        layout.addView(refresh);

        output = new TextView(this);
        output.setTextSize(16);
        output.setText("Pindutin ang button para basahin ang cellular information.");
        layout.addView(output);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(layout);
        setContentView(scroll);

        telephony = (TelephonyManager)
                getSystemService(TELEPHONY_SERVICE);

        refresh.setOnClickListener(v -> readCells());

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
            }, PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == PERMISSION_REQUEST) {
            output.setText("Permission result received. Pindutin ang READ CELL INFORMATION.");
        }
    }

    private void readCells() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            output.setText("Kailangan mong payagan ang Phone at Location permissions.");
            return;
        }

        try {
            List<CellInfo> cells = telephony.getAllCellInfo();

            if (cells == null || cells.isEmpty()) {
                output.setText("Walang cellular information na ibinigay ng Android. Tingnan kung naka-on ang SIM at Location.");
                return;
            }

            StringBuilder result = new StringBuilder();
            result.append("CELLULAR NETWORK INFORMATION\n\n");

            int count = 0;
            for (CellInfo cell : cells) {
                count++;
                result.append("Cell #").append(count).append("\n");

                CellIdentity id = null;
                CellSignalStrength signal = null;
                String type = "Unknown";
                String cellId = "Unavailable";
                String mcc = "Unavailable";
                String mnc = "Unavailable";

                if (cell instanceof CellInfoLte) {
                    CellInfoLte c = (CellInfoLte) cell;
                    id = c.getCellIdentity();
                    signal = c.getCellSignalStrength();
                    type = "LTE / 4G";
                    int ci = c.getCellIdentity().getCi();
                    if (ci != Integer.MAX_VALUE) cellId = String.valueOf(ci);
                    mcc = c.getCellIdentity().getMccString();
                    mnc = c.getCellIdentity().getMncString();

                } else if (cell instanceof CellInfoGsm) {
                    CellInfoGsm c = (CellInfoGsm) cell;
                    id = c.getCellIdentity();
                    signal = c.getCellSignalStrength();
                    type = "GSM";
                    int cid = c.getCellIdentity().getCid();
                    if (cid != Integer.MAX_VALUE) cellId = String.valueOf(cid);
                    mcc = c.getCellIdentity().getMccString();
                    mnc = c.getCellIdentity().getMncString();

                } else if (cell instanceof CellInfoWcdma) {
                    CellInfoWcdma c = (CellInfoWcdma) cell;
                    id = c.getCellIdentity();
                    signal = c.getCellSignalStrength();
                    type = "WCDMA / 3G";
                    int cid = c.getCellIdentity().getCid();
                    if (cid != Integer.MAX_VALUE) cellId = String.valueOf(cid);
                    mcc = c.getCellIdentity().getMccString();
                    mnc = c.getCellIdentity().getMncString();

                } else if (android.os.Build.VERSION.SDK_INT >= 29
                        && cell instanceof CellInfoNr) {
                    CellInfoNr c = (CellInfoNr) cell;
                    id = c.getCellIdentity();
                    signal = c.getCellSignalStrength();
                    type = "NR / 5G";
                    long nci = c.getCellIdentity().getNci();
                    if (nci != Integer.MAX_VALUE && nci != Long.MAX_VALUE)
                        cellId = String.valueOf(nci);
                    mcc = c.getCellIdentity().getMccString();
                    mnc = c.getCellIdentity().getMncString();

                } else if (android.os.Build.VERSION.SDK_INT >= 29
                        && cell instanceof CellInfoTdscdma) {
                    CellInfoTdscdma c = (CellInfoTdscdma) cell;
                    id = c.getCellIdentity();
                    signal = c.getCellSignalStrength();
                    type = "TD-SCDMA";
                    int cid = c.getCellIdentity().getCid();
                    if (cid != Integer.MAX_VALUE) cellId = String.valueOf(cid);
                    mcc = c.getCellIdentity().getMccString();
                    mnc = c.getCellIdentity().getMncString();
                }

                result.append("Type: ").append(type).append("\n");
                result.append("Cell ID: ").append(cellId).append("\n");
                result.append("MCC: ").append(mcc).append("\n");
                result.append("MNC: ").append(mnc).append("\n");
                result.append("Registered: ")
                      .append(cell.isRegistered() ? "Yes" : "No")
                      .append("\n");

                if (signal != null) {
                    result.append("Signal: ")
                          .append(signal.getDbm())
                          .append(" dBm\n");
                }

                result.append("\n");
            }

            output.setText(result.toString());

        } catch (SecurityException e) {
            output.setText("Permission denied. Payagan ang Phone at Location permissions sa Settings.");
        } catch (Exception e) {
            output.setText("Error: " + e.getClass().getSimpleName()
                    + "\n" + e.getMessage());
        }
    }
}
