var gotSettings = false;

/***************************************
 ******* Verkrijg instellingen   *******
 ***************************************/
function refreshSettings(boeveninterval) {
    // Builds a Fusion Tables SQL query and hands the result to dataHandler()
	var queryUrlHead = 'https://www.googleapis.com/fusiontables/v1/query?sql=';
	//var queryUrlHead = 'http://www.google.com/fusiontables/api/query?sql=';
    var queryUrlKey  = '&key=AIzaSyD9DNAkA31T6POZbZuoo6ypDOoAYSdeMoM';
    // write your SQL as normal, then encode it
    var query = "SELECT gameid, interval, tijd_zichtbaar, ROWID FROM 1LtYYxL7RCyZHj8mV3syeR7MkyGDlJbIBW7hFTmk WHERE id = '" + 
    				window.localStorage.getItem("setting_playerid") + "' AND " + 
                    "'location' NOT EQUAL TO " + (-1 * Math.floor(Math.random() * 10000000)).toString() + " LIMIT 100";
    var queryurl = encodeURI(queryUrlHead + query + queryUrlKey);
    
    $.getJSON(queryurl, function(data) {
    	window.localStorage.setItem("setting_gameID", 		data.rows[0][0]);
    	var interval = data.rows[0][1];
    	var visibleTime = data.rows[0][2];
    	window.localStorage.setItem("setting_interval",     interval);
    	window.localStorage.setItem("setting_visibleTime",  visibleTime);
    	window.localStorage.setItem("setting_rowID",        data.rows[0][3]);
    	gotSettings = true;
    	writePlayerSettingsFile();
    	boeveninterval(visibleTime, interval);
    }).error(function(jqXHR, textStatus, errorThrown) {
        console.log("error " + textStatus);
        console.log("incoming Text " + jqXHR.responseText);
    });
}

/***************************************
 ******* De kaart pagina (MAP)   *******
 ***************************************/
var griepummap;
var agentsinterval = false;
var boeveninterval = false;

$('#page-map').live("pageshow", function() {
	var spectator = false;
	// Check of playerID is ingesteld, vraag anders om ID
	if(window.localStorage.getItem("setting_playerid") == null ||
	   window.localStorage.getItem("setting_playerid") == "") {
		spectator = true;
		var playerID = prompt("Er is nog geen playerid ingesteld, voer die hier in:");
		if(playerID != null) {
			spectator = false;
			window.localStorage.setItem("setting_playerid", playerID);
			alert("Speler is opgeslagen.");
		}
	}
	
	if(spectator) {
		gotSettings = true;
		alert("Je speelt dit spel nu in bekijkmodus, je gegevens worden dus niet bijgewerkt! Je kunt ook geen boeven zien.");
	}
	
	/**
	 * Refresh de settings en set het refresh interval voor de Fusion Table layer voor de boeven
	 */
	if(!spectator) refreshSettings(function setBoevenRefreshIntervals(visibleTime, interval) {
		if(!boeveninterval) {
			setInterval(function(){
				if(boeven != null) boeven.setMap(null);
				boeven = createFusionTableLayer(boeven, "'type' = 1");
				boeven.setMap(griepummap);
				setTimeout(function(){
					boeven.setMap(null);
				}, visibleTime * 1000);
			}, interval * 1000 + visibleTime * 1000);
			boeveninterval = true;
		}
	});
	
	var agents;
	var boeven;
	var myposition = new google.maps.LatLng(52.260908,6.793399); // Standaard is Hengelo
	navigator.geolocation.getCurrentPosition(function(position) {
		// Gebruik de huidige positie van de gebruiker
		myposition = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
	},null);
	// Laad een kaart
	$('#map_canvas').gmap({
		'center' : myposition,
		'zoom' : 12,
		'mapTypeControl' : false,
		'navigationControl' : true,
		'streetViewControl' : false,
		'callback' : function() {
			// Sla de kaart in een lokale variabele op
			griepummap = this.get('map');
		}
	});
	
	// 	Set het refresh interval voor de Fusion Table layer voor de agenten
	if(!agentsinterval) {
		setInterval(function(){
			if(agents != null) agents.setMap(null);
			agents = createFusionTableLayer(agents, "'type' = 0");
			agents.setMap(griepummap);
		}, 10000);
		agentsinterval = true;
	}
});
$('#map_center').bind('click', function(event, ui) {
	navigator.geolocation.getCurrentPosition(
		function(position) {
			griepummap.setCenter(new google.maps.LatLng(position.coords.latitude, position.coords.longitude));
		},
		function() {
			alert('Je huidige locatie is onbekend, centreren is dus niet mogelijk.');
		}
	);
});


/***************************************
 ******* File handling functies  *******
 ***************************************/
function writePlayerSettingsFile() {
    window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, writeFileSystemCheck, fail);
}
function writeFileSystemCheck(fileSystem) {
    fileSystem.root.getFile("data/roc.griepum/player", {create: true}, writePlayerFileEntry, fail); 
}
function writePlayerFileEntry(fileEntry) {
	console.log(fileEntry.fullPath);
    fileEntry.createWriter(truncateFileWriter, fail);
    fileEntry.createWriter(writePlayerFile, fail);
}
function truncateFileWriter(writer) {
    writer.truncate(0);
    console.log("Settings file is now empty");
}
function writePlayerFile(writer) {
    writer.write(window.localStorage.getItem("setting_rowID"));
    console.log("Updated player ID in griepum settings file");
}
function fail(evt) {
    console.log(evt.target.error.code);
}

/**
 * Instellingen pagina
 */
$("#page-settings").live("pageshow", function() {
	$("#playerSettingsMessage").html("" +
		"De volgende instellingen zijn bekend:" +
		"<table id=\"settings-table\">" +
		"<tr><th>Speler ID</th><td>" + window.localStorage.getItem("setting_playerid") + "</td></tr>" +
		"<tr><th>Game ID</th><td>" + window.localStorage.getItem("setting_gameID") + "</td></tr>" +
		"<tr><th>Interval</th><td>" + window.localStorage.getItem("setting_interval") + "</td></tr>" +
		"<tr><th>Tijd zichtbaar</th><td>" + window.localStorage.getItem("setting_visibleTime") + "</td></tr>" +
		"</table>");
});

function resetSettings() {
   	window.localStorage.setItem("setting_playerid", 	"");
   	window.localStorage.setItem("setting_gameID", 		"");
   	window.localStorage.setItem("setting_interval", 	"");
   	window.localStorage.setItem("setting_visibleTime", 	"");
   	window.localStorage.setItem("setting_rowID",        "");
   	$("#playerSettingsMessage").text("De instellingen zijn gereset, start een nieuw spel om nieuwe instellingen op te halen");
}

/**
 * FusionTables helper
 * @param layer
 * @param table
 * @returns
 */
function createFusionTableLayer(layer, whereclause){
	console.log("Layer refreshing");
    layer = new google.maps.FusionTablesLayer({
        query: 
        {
            from: '1LtYYxL7RCyZHj8mV3syeR7MkyGDlJbIBW7hFTmk',
            select: 'location',
            where: "'timestamp' > " + (Math.round(((new Date()).getTime() / 1000)) - 600) + " AND " +
                   whereclause + " AND " +
                   "'gameid' = '" + window.localStorage.getItem("setting_gameID") + "' AND " + 
                   "'location' NOT EQUAL TO " + (-1 * Math.floor(Math.random() * 10000000)).toString(),
            limit: 1000
        }
    });
    return layer;
}
