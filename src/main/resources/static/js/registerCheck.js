let duplicateChecked = false;
let duplicateNicknameChecked = false;
let duplicateEmailChecked = false;
let mailAuthNumberChecked = false;
let Passwordform = false;
let PasswordCheck = false;



$(document).ready(function() {
    $("#sendVerificationEmailBtn").click(function() {
        const email = $("#email").val().trim();
        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");

        if (email === "") {
            alert("이메일을 입력해 주세요.")
            return;
        }

         const email_regex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/i;
            if (!email_regex.test(email)) {
                alert("이메일 형식을 지켜주세요.");
                return;
            }

        $.ajax({
            beforeSend: function (xhr) {
                       xhr.setRequestHeader(header, token);
                     },
            type: "POST",
            url: "/users/create/mail-auth",
            data: {
                email: email
            },
        })
            .done(function() {
                $("#verificationMessage").text("인증 메일이 전송되었습니다.");
            })
            .fail(function() {
                $("#verificationMessage").text("인증 메일 전송에 실패했습니다.");
            });
    });
});

// 인증번호 발송
function sendEmail() {
  const email = $("#email").val().trim(); // 사용자가 입력한 이메일 주소
   var token = $("meta[name='_csrf']").attr("content");
   var header = $("meta[name='_csrf_header']").attr("content");
  // 이메일칸이 비어있으면 알림
   if (email === "")
   {
       alert("이메일을 입력해 주세요.")
       return;
   }

   const email_regex = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/i;
   if (!email_regex.test(email)) {
         alert("이메일 형식을 지켜주세요.");
         return;
   }


  $.ajax({
      beforeSend: function (xhr) {
              xhr.setRequestHeader(header, token);
            },
      url: "/users/create/mail-auth",
      type: "POST",
      data: {
          "email": email
      },

      success: function(data) {
          // 이메일 전송 성공
          $("#email").prop("readonly", true);
          $("#duplicateCheckEamilButton").hide();
          $("#changeEmailButton").show();
          duplicateEmailChecked = true;
          alert("인증번호가 전송되었습니다.");
      },
      error: function() {
          // 이메일 전송 실패
          alert("인증번호 전송에 실패했습니다.\n 이미 가입한 이메일이거나 잘못된 이메일입니다.");
      }
  });
}

// 인증번호 확인
function verifyCode() {
  var token = $("meta[name='_csrf']").attr("content");
  var header = $("meta[name='_csrf_header']").attr("content");
  const inputCode = $("#inputCode").val().trim(); // 사용자가 입력한 인증번호
  if (inputCode === '') { // 인증번호입력없이 확인하기를 눌렀을 경우 알림
    alert("인증번호를 입력해주세요.");
    mailAuthNumberChecked = false;
    return;
  }

  $.ajax({
    beforeSend: function (xhr) {
                xhr.setRequestHeader(header, token);
              },
    url: "/users/mailCheck",
    type: "POST",
    data: {
      "inputCode": inputCode
    },

    success: function(isCodeCorrect) {
      if (isCodeCorrect) {
        alert("인증번호가 일치합니다.");
        mailAuthNumberChecked =true;
      } else {
        alert("인증번호가 일치하지 않습니다.");
        mailAuthNumberChecked = false;
        $("#inputCode").focus();
      }
    },
    error: function() {
      alert("인증번호 확인에 실패했습니다.");
      mailAuthNumberChecked = false;
    }
  });
}


function checkDuplicateUsername() {
    const username = $("#username").val().trim();
     var token = $("meta[name='_csrf']").attr("content");
     var header = $("meta[name='_csrf_header']").attr("content");

    if (username === "") {
        alert("아이디를 입력해주세요.");
        duplicateChecked = false;
        return;
    }

    // 영문 6자리 아이디 검사
    const idRegex = /^[a-zA-Z0-9]{5,15}$/;
    if (!idRegex.test(username)) {
        alert("아이디는 영문 또는 숫자 5자리 이상 15자리 이하로 입력해주세요.");
        duplicateChecked = false;
        return;
    }

    $.ajax({
        beforeSend: function (xhr) {
                 xhr.setRequestHeader(header, token);
        },
        url: "/users/create/check-duplicate-id",
        type: "POST",
        data: {username: username},

        success: function(response) {
            if (response) {
                if (confirm("사용 가능한 아이디입니다. 사용하시겠습니까?")) {
                    // 사용 버튼을 클릭한 경우
                    $("#username").prop("readonly", true);
                    $("#duplicateCheckButton").hide();
                    $("#changeButton").show();
                    duplicateChecked = true;
                }
            } else {
                alert("이미 사용 중인 아이디입니다.");
                duplicateChecked = false;
            }
        },
        error: function() {
            alert("중복 확인 중 오류가 발생했습니다.");
            duplicateChecked = false;
        }
    });
}

function checkDuplicateNickname() {

    const nickname = $("#nickname").val().trim();
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    if (nickname === "") {
            alert("닉네임을 입력해주세요.");
            duplicateNicknameChecked = false;
            return;
    }

    // 2자리 아이디 검사
    const idRegex = /^[a-zA-Z0-9ㄱ-ㅣ가-힣]{2,10}$/;

    if (!idRegex.test(nickname)) {
        alert("닉네임은 2자리이상 10자리 이하로 입력해주세요.");
        duplicateNicknameChecked = false;
        return;
    }

    $.ajax({
        beforeSend: function (xhr) {
                      xhr.setRequestHeader(header, token);
                    },
        url: "/users/create/check-duplicate-nickname",
        type: "POST",
        data: {nickname: nickname},


         success: function(response) {
            if (response) {
                if (confirm("사용 가능한 닉네임입니다. 사용하시겠습니까?")) {
                    // 사용 버튼을 클릭한 경우

                    $("#nickname").prop("readonly", true);
                    $("#duplicateCheckNicknameButton").hide();
                    $("#changeNicknameButton").show();
                    duplicateNicknameChecked = true;
                }
            } else {
                alert("이미 사용 중인 닉네임입니다.");
                duplicateNicknameChecked = false;
            }
        },
        error: function() {
            alert("중복 확인 중 오류가 발생했습니다.");
            duplicateNicknameChecked = false;
        }
    });
}

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
            Passwordform = false;
            passwordFormatError.show();

        } else {
            passwordInput.removeClass("error-input");
            Passwordform = true;
            passwordFormatError.hide();
        }
    });

    passwordConfirmInput.on("input", function() {
        const password = passwordInput.val();
        const passwordConfirm = passwordConfirmInput.val();

        if (password !== passwordConfirm) {
            passwordConfirmInput.addClass("error-input");
            PasswordCheck = false;
            passwordMatchError.show();
        } else {
            passwordConfirmInput.removeClass("error-input");
            PasswordCheck = true
            passwordMatchError.hide();
        }
    });
});




function validateForm() {
    if (!duplicateChecked) {
        alert("아이디 중복 확인을 완료해 주세요.");
        return false;
    }

    if(!duplicateNicknameChecked)
    {
         alert("닉네임 중복 확인을 완료해 주세요.");
         return false;
    }

    if(!duplicateEmailChecked)
    {
        alert("이메일 인증을 해주세요.");
        return false;
    }

    if(!mailAuthNumberChecked)
    {
         alert("인증번호를 확인해주세요.");
         return false;
    }


    if(!Passwordform)
    {
        alert("비밀번호 양식을 지켜주세요.");
        return false;
    }

     if(!PasswordCheck)
     {
         alert("비밀번호가 서로 맞는지 확인해주세요");
         return false;
     }


    alert("회원가입이 완료되었습니다");
    return true;
}


function changeusername() {
    $("#username").prop("readonly", false);
    $("#duplicateCheckButton").show();
    $("#changeButton").hide();
    duplicateChecked = false;
}

function changeNickname() {
    $("#nickname").prop("readonly", false);
    $("#duplicateCheckNicknameButton").show();
    $("#changeNicknameButton").hide();
    duplicateNicknameChecked = false;
}

function changeEmail()
{
     $("#email").prop("readonly", false);
     $("#duplicateCheckEamilButton").show();
     $("#changeEmailButton").hide();
     duplicateEmailChecked = false;
     mailAuthNumberChecked = false;
}