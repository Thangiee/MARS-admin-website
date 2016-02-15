// validate username and password login form fields
$(document).ready(function () {
    $('#login-form').validate({
        rules: {
            'login-user': {
                required: true,
                nowhitespace: true
            },
            'login-passwd': {
                required: true
            }
        },
        errorElement: "em",
        errorClass: "red-text"
    })
});