$(document).ready(function() {
  getListings();
});

async function getListings () {
  const query = '/explore';
  const response = await fetch(query);
  const json = await response.json();
  loadListings(json);
}

function loadListings (json) {
  const section = document.getElementById('club-listings');
  section.innerHTML = '';
  for (var index in json) {
    section.innerHTML += '<div class="club-listing">' + 
                           '<div class="club-logo-container">' + 
                             '<img src="images/image1.png" class="club-logo" alt="Club logo">' + 
                           '</div>' + 
                           '<div class="club-info-container">' + 
                             '<h2>' + json[index].name + '</h2>' + 
                             '<p>' + json[index].description + '</p>' + 
                             '<p>' + json[index].members.length + ' members</p>' +     
                           '</div>' + 
                           '<div class="club-join-container">' + 
                             '<button onclick="location.href=\'about-us.html\';">Join club</button>' + 
                           '</div>' + 
                         '</div>';
  }
}
