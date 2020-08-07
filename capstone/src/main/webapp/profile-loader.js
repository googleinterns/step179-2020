showOrHideProfile();

/** Fetch login status and show or hide profile accordingly */
function showOrHideProfile() {
  fetch('/auth').then(response => response.text()).then((loginStatus) => {
    if (loginStatus.includes('logout')) {
      var authContent = document.getElementById('top-navigation');
      authContent.innerHTML = '<a id="logout-url" href="">Logout</a>' + authContent.innerHTML;
      document.getElementById('logout-url').href = loginStatus;
      getStudentInfo();
    }
    else {
      document.getElementById('profile-content').innerHTML = loginStatus;
    }
  })
}

/** Fetch student information and add it to the profile */
function getStudentInfo() {
  fetch('/student-data').then(response => response.json()).then((info) => {  
    var studentInfo = info['student'];
    // Display profile name with edit icon
    const pencilIcon = '<i class="far fa-edit pencil-edit"></i>';
    document.getElementById('edit-name').innerHTML = studentInfo['name'];
    document.getElementById('heading').innerHTML += pencilIcon;

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
  const content = club + '  <button name="leave" value="' + club + '" formmethod="POST">Leave</button>';
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
    messageForm.action = imageUploadUrl;
  });
}
