getStudentInfo();

/** Fetch student information and add it to the profile */
function getStudentInfo() {
  fetch('/student-data').then(response => response.json()).then((info) => {  
    var studentInfo = info['student'];
    // Display profile name with edit icon
    const pencilIcon = '<i class="far fa-edit pencil-edit"></i>';
    document.getElementById('edit-name').innerHTML = studentInfo['name'];
    document.getElementById('profile-heading').innerHTML += pencilIcon;

    // Display profile club list
    getClubElements(studentInfo['clubs']);

    // Add additional student information
    document.getElementById('email').innerHTML += studentInfo['email'];
    document.getElementById('edit-year').innerHTML += studentInfo['gradYear'];
    document.getElementById('edit-major').innerHTML += studentInfo['major'];

    // Add announcements to student's inbox
    addAnnoucements(info['announcements']);

    // Upload profile picture
    if (studentInfo['profilePicture'] != '') {
      getImageUrl(studentInfo['profilePicture']);
    } else {
      document.getElementsByClassName('profile-pic')[0].src = 'images/profile.jpeg';
    }
  });
  getInterestedClubList();
}

function getInterestedClubList() {
  fetch('/interested-clubs').then(response => response.json()).then((interestedClubs) => {
    const template = document.querySelector('#interested-list');
    for(const club of interestedClubs) {
      template.content.querySelector('li').innerHTML = club;
      var clone = document.importNode(template.content, true);
      document.getElementById('interested-club-content').appendChild(clone);
    }
  });
}

/** Fill in inbox template with each club's announcements */
function addAnnoucements(announcements) {
  const template = document.querySelector('#inbox-list');
  for(const announcement of announcements){
    template.content.querySelector('li').innerHTML = announcement;
    var clone = document.importNode(template.content, true);
    document.getElementById('inbox').appendChild(clone);
  }
}

/** Fill in club list template with all club names and leave buttons */
function getClubElements(clubs) {
  const template = document.querySelector('#club-list');
  for(const club of clubs){
    template.content.querySelector('li').innerHTML = getClubContent(club);
    var clone = document.importNode(template.content, true);
    document.getElementById('club-content').appendChild(clone);
  }
}

function getClubContent(club) {
  const content = club + '  <button id="leave" name="leave" value="' + club + '" formmethod="POST">Leave</button>';
  return content;
}

/** Store edited content from profile page */
function saveProfileChanges() {
  const newYear = document.getElementById("edit-year").innerHTML;
  const newMajor = document.getElementById("edit-major").innerHTML;
  const newName = document.getElementById("edit-name").innerHTML;

  document.getElementsByName('new-year')[0].value = newYear;
  document.getElementsByName('new-major')[0].value = newMajor;
  document.getElementsByName('new-name')[0].value = newName;

  document.forms['edit-profile'].submit();
}

/** Get image URL with given key */
function getImageUrl(pictureKey) {
  fetch('/get-image?blobKey=' + pictureKey).then((pic) => {
    document.getElementsByClassName('profile-pic')[0].src = pic.url;
  });
}

/** Fetches blobstore image upload url. */
function fetchBlobstoreProfileUrl() {
  fetch('/blobstore-profile-url').then(response => response.text()).then((imageUploadUrl) => {  
    const messageForm = document.getElementById('profile-form');
    if (messageForm != null) {
      messageForm.action = imageUploadUrl;
    }
  });
}

/** Direct to Explore page once logged in */
function onSignIn(googleUser) {
  window.location.href = '/explore.html';
}
