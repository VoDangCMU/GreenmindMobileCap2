package com.vodang.greenmind.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vodang.greenmind.location.Location

data class GarbageMapPoint(val lat: Double, val lng: Double, val intensity: Int)

// Sample heatmap data around Da Nang, Viet Nam
val sampleGarbageMapPoints = listOf(
    GarbageMapPoint(16.0678, 108.2208, 4),  // Hải Châu — Trần Phú
    GarbageMapPoint(16.0544, 108.2022, 5),  // Hải Châu — Nguyễn Văn Linh
    GarbageMapPoint(16.0720, 108.2290, 3),  // Sơn Trà — Phạm Văn Đồng
    GarbageMapPoint(16.0800, 108.2150, 2),  // Thanh Khê — Điện Biên Phủ
    GarbageMapPoint(16.0610, 108.2100, 5),  // Hải Châu — Lê Duẩn
    GarbageMapPoint(16.0480, 108.2200, 3),  // Ngũ Hành Sơn — Lê Văn Hiến
    GarbageMapPoint(16.0900, 108.1980, 2),  // Liên Chiểu — Nguyễn Lương Bằng
    GarbageMapPoint(16.0750, 108.2350, 4),  // Sơn Trà — Hoàng Sa
    GarbageMapPoint(16.0580, 108.2450, 1),  // Ngũ Hành Sơn — biển Mỹ Khê
    GarbageMapPoint(16.0420, 108.2300, 3),  // Ngũ Hành Sơn — Trường Sa
    GarbageMapPoint(16.0820, 108.2080, 5),  // Thanh Khê — Hùng Vương
    GarbageMapPoint(16.0650, 108.1950, 2),  // Cẩm Lệ — Cách Mạng Tháng 8
)

@Composable
expect fun GarbageHeatMapView(
    points: List<GarbageMapPoint>,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier = Modifier,
)

fun buildLeafletHtml(
    points: List<GarbageMapPoint>,
    lat: Double,
    lng: Double,
    zoom: Double,
    localScripts: Boolean = false,
): String {
    val heatData = points.joinToString(",") { "[${it.lat},${it.lng},${it.intensity / 5.0}]" }
    val css = if (localScripts) "leaflet.css" else "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
    val js  = if (localScripts) "leaflet.js"  else "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
    val heat = if (localScripts) "leaflet-heat.js" else "https://unpkg.com/leaflet.heat@0.2.0/dist/leaflet-heat.js"
    return """<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
  <link rel="stylesheet" href="$css"/>
  <script src="$js"></script>
  <script src="$heat"></script>
  <style>
    html,body{margin:0;padding:0;width:100%;height:100%;}
    #map{width:100%;height:100%;}
  </style>
</head>
<body>
  <div id="map"></div>
  <script>
    var map = L.map('map',{zoomControl:false}).setView([$lat,$lng],$zoom);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',{
      maxZoom:19, attribution:'© OSM contributors'
    }).addTo(map);
    var pts=[$heatData];
    if(pts.length>0){
      L.heatLayer(pts,{
        radius:45,blur:25,minOpacity:0.4,
        maxZoom:0,
        gradient:{0.0:'#2e7d32',0.3:'#66bb6a',0.6:'#ffeb3b',0.8:'#ff9800',1.0:'#d32f2f'}
      }).addTo(map);
    }
    var userDot=null;
    function updateView(lat,lng,zoom){
      map.setView([lat,lng],zoom,{animate:true});
      if(userDot){userDot.setLatLng([lat,lng]);}
      else{userDot=L.circleMarker([lat,lng],{radius:9,color:'#fff',weight:2,fillColor:'#1976D2',fillOpacity:1}).addTo(map);}
    }
  </script>
</body>
</html>"""
}
