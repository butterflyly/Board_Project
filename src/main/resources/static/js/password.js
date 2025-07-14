$(document).ready(function() {
    const passwordInput = $("#passwordInput");
    const passwordConfirmInput = $("#passwordConfirm");
    const passwordFormatError = $("#passwordFormatError");
    const passwordMatchError = $("#passwordMatchError");

    passwordInput.on("input", function() {
        const password = passwordInput.val();
        const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d).{12,}$/;

        if (!passwordRegex.test(password)) {
            passwordInput.addClass("error-input");
            passwordFormatError.show();
        } else {
            passwordInput.removeClass("error-input");
            passwordFormatError.hide();
        }
    });

    passwordConfirmInput.on("input", function() {
        const password = passwordInput.val();
        const passwordConfirm = passwordConfirmInput.val();

        if (password !== passwordConfirm) {
            passwordConfirmInput.addClass("error-input");
            passwordMatchError.show();
        } else {
            passwordConfirmInput.removeClass("error-input");
            passwordMatchError.hide();
        }
    });
});