/**
 * utils functions
 * **/


//Global variable that holds token
var token=""

function registerMenuItem(id, uri,errorVue) {

    $(id).click(function () {

        $.ajax({
            url: uri,
            type: "GET",
            headers: { 'X-Auth-Token': token},
            success: function (data) {
                $('#main').html(data)

            },
            error: function (data) {
                console.log("error")
                errorVue.message = "login and password are not authorized"
                errorVue.isError = true;
            }
        })
    })

}


