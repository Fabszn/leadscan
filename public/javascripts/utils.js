/**
 * utils functions
 * **/


function registerMenuItem(id, uri) {

    $(id).click(function () {
        $.get(uri).fail(function () {
            console.error('error on click ' + id)
        }).success(function (data) {
            $('#main').html(data)
        })
    })

}


