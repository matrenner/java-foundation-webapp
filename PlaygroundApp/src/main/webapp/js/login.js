$(document).foundation();

document.getElementById('postLoginData').addEventListener('submit', postLoginData);

 function postLoginData(event){
    event.preventDefault();

    $('#loginErrorText').css('display', 'none');

    let user = document.getElementById('usernameOrEmail').value;
    let pw = document.getElementById('password').value;

    fetch('/PlaygroundApp/login', {

        headers: {
            "Content-Type": "application/json"
        },
        method: 'POST',
        body: JSON.stringify({usernameOrEmail:user, password:pw})

    }).then(response => {
        if (response.ok) {
            return response.json();
        } else {
            Promise.reject(response.json());
        }
    }).then((responseJson) => {
        console.log(responseJson);

        if (responseJson.isValid) {
            localStorage.setItem('token', responseJson.token);
            window.location.href = "/PlaygroundApp/foundation";
        } else {
            $('#loginErrorText').css('display', 'inline');
        }
    }).catch((error) => {
        console.log(error);
    });
}