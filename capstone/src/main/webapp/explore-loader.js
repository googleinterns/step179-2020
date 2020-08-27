async function loadExplore () {
  getListings();
  loadLabels();
}

async function loadLabels () {
  const listElement = document.getElementById('labels-select-options');

  const query = '/labels';
  const response = await fetch(query);
  const labels = await response.json();
  for (var label of labels) {
    listElement.innerHTML += '<option value="' + label + '">';
  }
}

async function addFilterAndReload () {
  const filter = document.getElementById('labels-select').value;
  document.getElementById('labels').innerHTML += '<li>' + filter + '</li>';
  document.getElementById('labels-select').value = '';
  getListings();
}

async function getListings () {
  const sortType = document.getElementById('sort-type').value;
  const labels = getLabelQueryString();

  var query = '/explore?sort=' + sortType + '&labels=' + labels;
  const response = await fetch(query);
  const json = await response.json();
  loadListings(json.clubs, json.studentClubs, json.studentInterestedClubs);
}

async function loadListings (clubs, studentClubs, interestedClubs) {
  document.getElementById('club-listings').innerHTML = ''; // Clear the listings div, for if we're refreshing the listings
  const template = document.querySelector('#club-listing');
  for (var club of clubs) {
    imageUrl = 'images/logo.png';
    if (club.logo != '') {
      imageUrl = await getImageUrl(club.logo);
    }
    template.content.querySelector('#club-logo').src = imageUrl;
    template.content.querySelector('#club-name').innerHTML = club.name;
    template.content.querySelector('#club-name').href = 'about-us.html?name=' + club.name;
    template.content.querySelector('#description').innerHTML = club.description;
    template.content.querySelector('#exclusive').innerHTML = club['exclusive'] ? 'Exclusive' : '';
    template.content.querySelector('#members').innerHTML = club.members.length + ' members';
    template.content.querySelector('.join-button').value = club.name;
    if (club['exclusive']) {
      template.content.querySelector('.join-button').innerText = 'Request to Join';
    }
    template.content.querySelector('.interested-join-button').value = club.name;
    var clone = document.importNode(template.content, true);
    document.getElementById('club-listings').appendChild(clone);
    addAlertOnclick('join-button', club.name);
    addAlertOnclick('interested-join-button', club.name);
    editButton(club.name, studentClubs, 'join-button', club['exclusive']);
    editButton(club.name, interestedClubs, 'interested-join-button', club['exclusive']);
  }
}

function addAlertOnclick(className, clubName) {
  var allButtons = document.getElementsByClassName(className);
  for (button of allButtons) {
      if (button.value == clubName) {
        if (button.text == 'Request to Join') {
          alert('You have requested to join ' + clubName + '. You will become a member of this club upon officer approval!');
        } else {
          button.onclick = className.includes('interested') 
              ? function() {sendJoinedAlert('interested', false, clubName)} 
              : function() {sendJoinedAlert('joined', false, clubName)};
        }
      }
  }
}

function editButton(club, joinedClubList, className) {
  if (!joinedClubList.includes(club)) {
    return;
  }
  var allButtons = document.getElementsByClassName(className);
  for (button of allButtons) {
    if (button.value == club) {
      // Change to type button so we do not submit the form
      button.type = 'button'; 
      
      // Revert to basic colors to show difference between clubs joined and clubs not joined
      button.style.backgroundColor = 'revert';
      button.style.color = '#000';
      button.style.opacity = 'revert';

      // Update onclick functions
      button.onclick = className.includes('interested') 
          ? function() {sendJoinedAlert('interested', true, club)} 
          : function() {sendJoinedAlert('joined', true, club)};
    
      if (button.innerText == 'Join Club' || button.innerText == 'Request to Join') {
        button.innerText = 'Joined';
      }
    }
  }
}

function getLabelQueryString () {
  const element = document.getElementById('labels');
  const labels = element.getElementsByTagName('li');
  var queryString = '';
  for (var label of labels) {
    queryString += label.innerText + ',';
  }
  return queryString.slice(0, -1); // Takes out the last comma. 
}

function sendJoinedAlert(interestedOrJoin, alreadyJoined, clubName) {
  var alertMessage = 'You have ';
  alertMessage += alreadyJoined ? 'already ' : 'successfully ';
  if (interestedOrJoin == 'interested') {
    alertMessage += 'expressed interest in insert_club! You can view your interested club list on your profile page'.replace('insert_club', clubName);
  } else {
    alertMessage += 'joined insert_club! You can view your club list or leave a club on your profile page. You will receive notifications for insert_club'
        .replace('insert_club', clubName)
        .replace('insert_club', clubName);
  }
  alert(alertMessage);
}
