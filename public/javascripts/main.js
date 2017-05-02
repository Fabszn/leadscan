function isNoError(classId) {
    var isError = false;
    _.each($('.' + classId), function (r) {
        if ($(r).val() == "" || $(r).val() == null) {
            isError = true;
            $(r).addClass('inputError');
            eVue.isError = true;
            eVue.message = "Please fill missed field(s))";
        } else {
            $(r).removeClass('inputError');
        }

        if (!isError) {
            eVue.isError = false;
        }
    });
    return !isError
}

