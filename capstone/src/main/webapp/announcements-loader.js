$(document).ready(function() {
  loadAnnouncements();
});

async function loadAnnouncements () {
  const query = "/announcements";
  const response = await fetch(query);
  const json = await response.json();

  const announcementsSection = document.getElementById('announcements-display');
  announcementsSection.innerHTML = '<h1>Announcements</h1>';
  announcementsSection.innerHTML += '<ul>';
  for (var index in json) {
    announcementsSection.innerHTML += '<li>'+json[index]+'</li>';
  }
  announcementsSection.innerHTML += '</ul>';
}

