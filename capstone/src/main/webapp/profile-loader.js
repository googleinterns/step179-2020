getStudentInfo();

/** Fetch student information and add it to the profile */
function getStudentInfo() {
  fetch('/student-data').then(response => response.json()).then((info) => {  
    var studentInfo = info['student'];
    // Display profile name with edit icon
    const pencilIcon = '<i id="pencil-name" class="far fa-edit pencil-edit"></i>';
    document.getElementById('edit-name').innerHTML = studentInfo['name'];
    document.getElementById('profile-heading').innerHTML += pencilIcon;
    document.getElementById('pencil-name').onclick= function() {sendEditAlert('name')};

    // Display profile club list
    displayElements(studentInfo['clubs'], '#club-list', 'club-content');

    // Add additional student information
    document.getElementById('email').innerHTML += studentInfo['email'];
    document.getElementById('edit-year').innerHTML += studentInfo['gradYear'];
    document.getElementById('edit-major').innerHTML += studentInfo['major'];

    // Add announcements to student's inbox
    displayElements(info['announcements'], '#inbox-list', 'inbox');

    // Upload profile picture
    if (studentInfo['profilePicture'] != '') {
      getImageUrl(studentInfo['profilePicture']);
    } else {
      document.getElementsByClassName('profile-pic')[0].src = 'images/profile.jpeg';
    }
  });
  getInterestedClubList();
}

function sendEditAlert(type) {
  alert('Hover over and click on your [type] to edit. Be sure to click "Save Changes" when you are done!'.replace('[type]', type));
}

/** Fetch interested club list and display on profile page */
function getInterestedClubList() {
  fetch('/interested-clubs').then(response => response.json()).then((interestedClubs) => {
    displayElements(interestedClubs, '#interested-list', 'interested-club-content');
  });
}

/** Fill list templates with necessary information */
function displayElements(items, templateId, documentId) {
  const template = document.querySelector(templateId);
  for(const item of items){
    if (templateId == '#club-list') {
      template.content.querySelector('li').innerHTML = getClubContent(item);
    } else {
      template.content.querySelector('li').innerHTML = item;
    }
    var clone = document.importNode(template.content, true);
    document.getElementById(documentId).appendChild(clone);
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
function onSignIn() {
  window.location.href = '/explore.html';
}
