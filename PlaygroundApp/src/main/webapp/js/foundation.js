$(document).foundation();

function callPlayServlet(element) {

    let data = {};

    data.status = "none";

    fetch('/PlaygroundApp/play',
    {

        headers: {
            'Content-Type': 'application/json',
            'Authorization': localStorage.getItem('token')
        },
        method: 'POST',
        body: JSON.stringify(data)

    }).then(function(response) {
        console.log(response);
    }).catch(function(error) {
        console.log(error);
    });
}