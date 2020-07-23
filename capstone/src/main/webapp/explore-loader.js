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
                             '<img src="images/logo.png" class="club-logo" alt="Club logo">' + 
                           '</div>' + 
                           '<div class="club-info-container">' + 
                             '<h2><a href="about-us.html">' + json[index].name + '</a></h2>' + 
                             '<p>' + json[index].description + '</p>' + 
                             '<p>' + json[index].members.length + ' members</p>' +     
                           '</div>' + 
                           '<div class="club-join-container">' + 
                             '<form action="student-data" method="POST">' + 
                               '<input type="hidden" name = "club" value="' + json[index].name + '">' + 
                               '<input type="hidden" name = "action" value="join">' + 
                               '<input type="submit" value="Join">' +
                             '</form>' + 
                             '<button onclick="location.href=\'about-us.html\';">Join club</button>' + 
                           '</div>' + 
                         '</div>';
  }
}
