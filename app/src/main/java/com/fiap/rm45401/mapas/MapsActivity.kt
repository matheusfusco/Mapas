package com.fiap.rm45401.mapas

import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import android.content.DialogInterface
import android.Manifest.permission
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.util.Log


class MapsActivity : AppCompatActivity(),
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mGoogleApiClient: GoogleApiClient
    val REQUEST_GPS = 51

    override fun onConnected(p0: Bundle?) {
        checkPermission()

        val myLocation = LocationServices
                .FusedLocationApi
                .getLastLocation(mGoogleApiClient)
        if (myLocation != null) {
            addMarker(myLocation.latitude, myLocation.longitude, "Minha localização.")
        }
    }

    override fun onConnectionSuspended(p0: Int) {
        Log.i("TAG", "Suspenso")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("TAG", "Erro de conexao")
    }

    @Synchronized fun callConnection() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build()
        mGoogleApiClient.connect()
    }

    private fun checkPermission() {
        val permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("", "Permissão para gravar negada")

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                val builder = AlertDialog.Builder(this)

                builder.setMessage("Necessária a permissao para GPS")
                        .setTitle("Permissao Requerida")

                builder.setPositiveButton("OK") { dialog, id ->
                    Log.i("TAG", "Clicked")
                    requestPermission()
                }

                val dialog = builder.create()
                dialog.show()

            } else {
                requestPermission()
            }
        }
    }

    protected fun requestPermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_GPS)
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_GPS -> {
                if (grantResults.size == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("TAG", "Permissão negada pelo usuário")
                } else {
                    Log.i("TAG", "Permissao concedida pelo usuario")
                }
                return
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btPesquisar.setOnClickListener {
            mMap.clear()

            val geocoder = Geocoder(this)
            var address: List<Address>?

            address = geocoder
                    .getFromLocationName(
                            etEndereco.text.toString(),
                            1
                    )

            if (address.isNotEmpty()) {
                val location = address[0]
                addMarker(location.latitude, location.longitude, location.locality)
            }
            else {
                var alert = AlertDialog.Builder(this).create()
                alert.setTitle("Ops!")
                alert.setMessage("Endereço não encontrado!")
                alert.setCancelable(true)
                etEndereco.text.clear()
                alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", {
                    dialogInterface, inteiro ->
                    alert.dismiss()
                })
                alert.show()
            }

        }
    }

    fun addMarker(latitude: Double, longitude: Double, title: String) {
        val markerPos = LatLng(-latitude, longitude)
        mMap.addMarker(MarkerOptions()
                .position(markerPos)
                .title(title)
                .icon(BitmapDescriptorFactory
                        .fromResource(R.mipmap.ic_launcher))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerPos, 16f))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        callConnection()
        // Add a marker in Sydney and move the camera
    }
}
