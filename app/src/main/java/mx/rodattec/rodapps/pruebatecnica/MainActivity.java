package mx.rodattec.rodapps.pruebatecnica;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import mx.rodattec.rodapps.pruebatecnica.library.HttpRequest;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;

    private String zipCode;

    private DownloadTask mDownloadTask;

    private List<LatLng> latLngList = new ArrayList<>();
    private List<Marker> markerList = new ArrayList<>();

    private Polygon polygon = null;

    private Marker centralMarker;

    private AlertDialog progressDialog;

    @BindView(R.id.editTextZipCode)
    EditText editTextZipCode;
    @BindView(R.id.editTextCountry)
    EditText editTextCountry;
    @BindView(R.id.editTextEntity)
    EditText editTextEntity;
    @BindView(R.id.editTextCity)
    EditText editTextCity;
    @BindView(R.id.editTextMunicipality)
    EditText editTextMunicipality;
    @BindView(R.id.editTextSuburb)
    EditText editTextSuburb;

    @BindColor(R.color.polygonStrokeColor)
    int polygonStrokeColor;
    @BindColor(R.color.fillPolygonColor)
    int fillPolygonColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) { /*m.onCreate*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Uso butterknife ya que considero que de esta forma queda un código más limpio*/
        ButterKnife.bind(this);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        progressDialog = new SpotsDialog.Builder()
                .setContext(MainActivity.this)
                .setMessage(R.string.download)
                .setCancelable(false)
                .build();

        mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mDownloadTask = null;

        textWatcherZipCode();
    } /*m.onCreate*/


    @Override
    public void onMapReady(GoogleMap googleMap) { /*m.onMapReady*/
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true); //Para activar el + y el - del zoom en el mapa
    } /*m.onMapReady*/

    private void textWatcherZipCode() { /*m.textWatcherZipCode*/

        editTextZipCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { /*m.beforeTextChanged*/

            } /*m.beforeTextChanged*/

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { /*m.onTextChanged*/
                resetFields();
                if(editTextZipCode.length() == 5) { /*1.*/
                    zipCode = editTextZipCode.getText().toString();
                    String[] url = {"https://sepomex-wje6f4jeia-uc.a.run.app/api/zip-codes/" + zipCode
                            , "https://poligonos-wje6f4jeia-uc.a.run.app/zip-codes/" + zipCode};
                    startDownload(url);
                } /*1.*/
            } /*m.onTextChanged*/

            @Override
            public void afterTextChanged(Editable s) { /*m.afterTextChanged*/

            } /*m.afterTextChanged*/
        });

    } /*m.textWatcherZipCode*/

    private void resetFields() { /*m.resetFields*/

        if(polygon != null) { /*1.*/
            polygon.remove();
        } /*1.*/

        for(Marker marker : markerList) { /*2.*/
            marker.remove();
        } /*2.*/

        if(centralMarker != null) { /*3.*/
            centralMarker.remove();
        } /*3.*/

        latLngList.clear();
        markerList.clear();
        editTextCountry.setText("");
        editTextEntity.setText("");
        editTextCity.setText("");
        editTextMunicipality.setText("");
        editTextSuburb.setText("");
    } /*m.resetFields*/

    private void startDownload(String[] url) { /*m.startDownload*/
        if( mDownloadTask == null || mDownloadTask.getStatus() != AsyncTask.Status.RUNNING ) { /*1.*/
            editTextZipCode.setEnabled(false);
            mDownloadTask = new DownloadTask();

            mDownloadTask.execute(url);
        } /*1.*/
        else { /*1.*/
            Toast.makeText(this, getString(R.string.download_already), Toast.LENGTH_SHORT).show();
        } /*1.*/
    } /*m.startDownload*/

    private void processJSON(String[] strInput) { /*m.processJSON*/

        try { /*try 1*/
            JSONObject jsonObject =  new JSONObject(strInput[0]);
            JSONObject entityObject = jsonObject.getJSONObject("federal_entity");
            JSONObject municipalityObject = jsonObject.getJSONObject("municipality");
            JSONArray suburbsArray = jsonObject.getJSONArray("settlements");

            editTextCountry.setText(getString(R.string.mexico));

            String entity = entityObject.getString("name");
            editTextEntity.setText(entity);

            String city = jsonObject.getString("locality");
            editTextCity.setText(city);

            String municipality = municipalityObject.getString("name");
            editTextMunicipality.setText(municipality);

            String[] suburb = new String[suburbsArray.length()];
            for(int i = 0; i < suburb.length; i++) { /*1.*/

                JSONObject suburbObject = suburbsArray.getJSONObject(i);
                suburb[i] = suburbObject.getString("name");
                if(suburbsArray.length() == 1) { /*1 colonia*/
                    editTextSuburb.append(suburb[i]);
                } /*1 colonia*/
                else { /*2 o más colonias*/
                    String suburbNum;
                    if(i == suburbsArray.length() - 1) { /*última colonia*/
                        suburbNum = String.valueOf(i + 1);
                        editTextSuburb.append(suburbNum + ".- " + suburb[i]);
                    } /*última colonia*/
                    else { /*quedan más colonias*/
                        suburbNum = String.valueOf(i + 1);
                        editTextSuburb.append(suburbNum + ".- " + suburb[i] + "\n");
                    } /*quedan más colonias*/
                } /*2 o más colonias*/
            } /*1.*/

            if(city.equals("CIUDAD DE MEXICO")) { /*2.*/
                JSONObject jsonObject1 = new JSONObject(strInput[1]);
                JSONObject geometryObject = jsonObject1.getJSONObject("geometry");
                JSONArray coordinatesArray = geometryObject.getJSONArray("coordinates"); //Quitar primer [
                JSONArray coordinatesNestedArray = coordinatesArray.getJSONArray(0); //Quitar segundo [
                int coordinatesLength = coordinatesNestedArray.length();
                JSONArray latLngArray; //quitar tercer [
                String[] latLngString = new String[2];
                double latitude;
                double longitude;
                LatLng latLng;

                for(int i = 0; i < coordinatesLength; i++) { /*2.1*/
                    latLngArray = coordinatesNestedArray.getJSONArray(i);
                    latLngString[0] = latLngArray.optString(0);
                    latLngString[1] = latLngArray.optString(1);
                    longitude = Double.parseDouble(latLngString[0]); //El primer valor del JSONArray corresponde a la longitud
                    latitude = Double.parseDouble(latLngString[1]); //El segundo valor del JSONArray corresponde a la latitud
                    latLng = new LatLng(latitude, longitude);
                    drawMarker(latLng);
                    latLngList.add(latLng);
                } /*2.1*/
                drawPolygon();
                getCentralMarker(latLngList);
                setCentralMarker(latLngList);
            } /*2.*/
            else { /*2.*/
                showZipCodeDialog();
            } /*2.*/

            progressDialog.dismiss();
        } /*try 1*/
        catch (JSONException e) { /*catch 1.1*/
            e.printStackTrace();
        } /*catch 1.1*/
    } /*m.processJSON*/

    private void showZipCodeDialog() { /*m.showZipCodeDialog*/

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this
                , R.style.AlertDialogTheme)
                .setTitle(R.string.zip_code);

        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.zip_code_mexico_city_dialog
                , (ViewGroup) findViewById(R.id.layoutZipCodeDialogContainer));
        builder.setView(view);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { /*m.onClick*/
            } /*m.onClick*/
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    } /*m.showZipCodeDialog*/

    private void drawPolygon() { /*m.drawPolygon*/

        //Create PolygonOptions
        PolygonOptions polygonOptions = new PolygonOptions().addAll(latLngList).clickable(true);

        polygon = mMap.addPolygon(polygonOptions);
        //Set Polygon stroke color
        polygon.setStrokeColor(polygonStrokeColor);
        polygon.setFillColor(fillPolygonColor);
    } /*m.drawPolygon*/

    private void drawMarker(LatLng latLng) { /*m.drawMarker*/

        MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_point));

        //Create Marker
        Marker marker = mMap.addMarker(markerOptions);

        //Add marker to list
        markerList.add(marker);
    } /*m.drawMarker*/

    private LatLng getCentralMarker(List<LatLng> latLngList) { /*m.getCentralMarker*/
        double[] centralMarker = {0.0, 0.0};

        for(int i = 0; i < latLngList.size(); i++) {
            centralMarker[0] += latLngList.get(i).latitude;
            centralMarker[1] += latLngList.get(i).longitude;
        }

        int totalMarkers = latLngList.size();

        return new LatLng(centralMarker[0] / totalMarkers, centralMarker[1] / totalMarkers);
    } /*m.getCentralMarker*/

    private void setCentralMarker(List<LatLng> latLngList) { /*m.setCentralMarker*/
        centralMarker = mMap.addMarker(new MarkerOptions().position(getCentralMarker(latLngList)));

        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(getCentralMarker(latLngList))
                        .zoom(14f)
                        .build()
        ));
    } /*m.setCentralMarker*/

    private class DownloadTask extends AsyncTask<String[], Void, String[]> {
        @Override
        protected void onPreExecute() { /*m.onPreExecute*/
            progressDialog.show();
        } /*m.onPreExecute*/

        @Override
        protected String[] doInBackground(String[]... strings) { /*m.doInBackground*/
            HttpRequest httpRequest = new HttpRequest();
            return httpRequest.getHttpRequest(strings[0]);
        } /*m.doInBackground*/

        @Override
        protected void onPostExecute(String[] str) { /*m.onPostExecute*/
            editTextZipCode.setEnabled(true);
            if(isEmptyStringArray(str)) { /*1.*/
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, getString(R.string.downloading_error)
                        , Toast.LENGTH_SHORT).show();
            } /*1.*/
            else { /*1.*/
                processJSON(str);
            } /*1.*/
        } /*m.onPostExecute*/

        @Override
        protected void onCancelled() { /*m.onCancelled*/
            editTextZipCode.setEnabled(true);
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, getString(R.string.downloading_canceled), Toast.LENGTH_SHORT)
                    .show();
        } /*m.onCancelled*/

        private boolean isEmptyStringArray(String[] str) { /*m.isEmptyStringArray*/
            for (String s : str) { /*1.*/
                if (s != null) { /*1.1*/
                    return false;
                } /*1.1*/
            } /*1.*/
            return true;
        } /*m.isEmptyStringArray*/

    } //Fin clase DownloadTask


} //Fin activity MainActivity