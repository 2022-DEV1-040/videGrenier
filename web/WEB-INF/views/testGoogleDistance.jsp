<!DOCTYPE html>
<html>
<head>
    <title>Distance Matrix Service</title>
    <style>
        #right-panel {
            font-family: 'Roboto','sans-serif';
            line-height: 30px;
            padding-left: 10px;
        }

        #right-panel select, #right-panel input {
            font-size: 15px;
        }

        #right-panel select {
            width: 100%;
        }

        #right-panel i {
            font-size: 12px;
        }
        html, body {
            height: 100%;
            margin: 0;
            padding: 0;
        }
        #map {
            height: 100%;
            width: 50%;
        }
        #right-panel {
            float: right;
            width: 48%;
            padding-left: 2%;
        }
        #output {
            font-size: 11px;
        }
    </style>
</head>
<body>
<div id="right-panel">
    <div id="inputs">
        <pre>
var origin1 = {lat: 55.930, lng: -3.118};
var origin2 = 'Greenwich, England';
var destinationA = 'Stockholm, Sweden';
var destinationB = {lat: 50.087, lng: 14.421};
        </pre>
    </div>
    <div>
        <strong>Results</strong>
    </div>
    <div id="output"></div>
</div>
<div id="map"></div>
<script>
    function initMap() {
        var markersArray = [];

        var origin1 = {lat: 55.93, lng: -3.118};
        var origin2 = 'Greenwich, England';
        var destinationA = 'Stockholm, Sweden';
        var destinationB = {lat: 50.087, lng: 14.421};

        var service = new google.maps.DistanceMatrixService;
        service.getDistanceMatrix({
            origins: [origin1, origin2],
            destinations: [destinationA, destinationB],
            travelMode: 'DRIVING',
            unitSystem: google.maps.UnitSystem.METRIC,
            avoidHighways: false,
            avoidTolls: false
        }, function(response, status) {
            if (status !== 'OK') {
                alert('Error was: ' + status);
            } else {
                var originList = response.originAddresses;
                var destinationList = response.destinationAddresses;
                var outputDiv = document.getElementById('output');
                outputDiv.innerHTML = '';


                for (var i = 0; i < originList.length; i++) {
                    var results = response.rows[i].elements;
                    for (var j = 0; j < results.length; j++) {
                        outputDiv.innerHTML += originList[i] + ' to ' + destinationList[j] +
                            ': ' + results[j].distance.text + ' in ' +
                            results[j].duration.text + '<br>';
                    }
                }
            }
        });
    }


</script>
<script async defer
        src="https://maps.googleapis.com/maps/api/js?key=AIzaSyCBaebZ5i6230uvRvSiAqecuRv_pEqnXcA&callback=initMap">
</script>
</body>
</html>