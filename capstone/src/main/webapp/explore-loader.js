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
  const template = document.querySelector('#club-listing');
  for (var club of json) {
    template.content.querySelector('#club-logo').src = 'images/logo.png';
    template.content.querySelector('#club-name').innerHTML = club.name;
    template.content.querySelector('#club-name').href = 'about-us.html?name=' + club.name;
    template.content.querySelector('#description').innerHTML = club.description;
    template.content.querySelector('#members').innerHTML = club.members.length + ' members';
    var clone = document.importNode(template.content, true);
    document.getElementById('club-listings').appendChild(clone);    
  }
}

