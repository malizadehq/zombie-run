var Entity = Class.create({
  initialize: function(map, location) {
    this.map = map;
    this.location = location;
    this.marker = this.getMarker(this.location);
  },
  
  getMarker: function(latLng) {
    alert("please override me.");
  },
  
  locationChanged: function(latLng) {
    this.location = latLng;
    this.marker.set_position(this.location);
  },
});

var Player = Class.create(Entity, {
  getMarker: function(latLng) {
    var markerimage = new google.maps.MarkerImage(
        "res/icon.png",
        new google.maps.Size(48, 48));
    return new google.maps.Marker({
        position:this.location,
        map:this.map,
        title:"You",
        icon:markerimage,
        // TODO: shadow image.
      });
  },
});

var Zombie = Class.create(Entity, {
  getMarker: function(latLng) {
    var markerimage = new google.maps.MarkerImage(
        "res/zombie_meandering.png",
        new google.maps.Size(14, 30));
    return new google.maps.Marker({
        position:this.location,
        map:this.map,
        title:"Zombie",
        icon:markerimage,
        // TODO: shadow image.
      });
  },
});