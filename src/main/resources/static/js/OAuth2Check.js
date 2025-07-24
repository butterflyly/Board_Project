let duplicateChecked = false;
let duplicateNicknameChecked = false;
let duplicateEmailChecked = false;
let mailAuthNumberChecked = false;


// 인증번호 발송
function sendEmail() {
  const email = $("#email").val().trim(); // 사용자가 입력한 이메일 주소
  const email_check = document.getElementById("email_Check").value
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
      url: "/OAuth2/create/mail-auth",
      type: "POST",
      data: {
          "email": email
      },

      success: function(data) {

          $("#email").prop("readonly", true);
          $("#duplicateCheckEamilButton").hide();
          $("#changeEmailButton").show();
          // 이메일 전송 성공
          if(email === email_check)
          {
            mailAuthNumberChecked = true;
            alert("본인인증 되었습니다.");
          }
          else
          {
            $("#div_invisible").show();
            alert("인증번호가 전송되었습니다.");

          }
          duplicateEmailChecked = true;
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



function validateForm() {

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


    alert("회원가입이 완료되었습니다");
    return true;
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
     $("#div_invisible").hide();
     duplicateEmailChecked = false;
     mailAuthNumberChecked = false;
}