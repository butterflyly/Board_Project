let duplicateChecked = false;
let duplicateNicknameChecked = false;


function checkDuplicateUserId() {
    const userId = $("#userId").val().trim();

    if (userId === "") {
        alert("아이디를 입력해주세요.");
        duplicateChecked = false;
        return;
    }

    // 영문 6자리 아이디 검사
    const idRegex = /^[a-zA-Z0-9]{5,15}$/;
    if (!idRegex.test(userId)) {
        alert("아이디는 영문 또는 숫자 5자리 이상 15자리 이하로 입력해주세요.");
        duplicateChecked = false;
        return;
    }

    $.ajax({
        url: "/register/check-duplicate-id",
        type: "POST",
        data: {userId: userId},

        success: function(response) {
            if (response) {
                if (confirm("사용 가능한 아이디입니다. 사용하시겠습니까?")) {
                    // 사용 버튼을 클릭한 경우
                    $("#userId").prop("readonly", true);
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

    if (nickname === "") {
            alert("닉네임을 입력해주세요.");
            duplicateNicknameChecked = false;
            return;
    }

    // 2자리 아이디 검사
    const idRegex = /^[a-zA-Z0-9]{2,10}$/;

    if (!idRegex.test(nickname)) {
        alert("닉네임은 2자리이상 10자리 이하로 입력해주세요.");
        duplicateNicknameChecked = false;
        return;
    }

    $.ajax({
        url: "/register/check-duplicate-nickname",
        type: "POST",
        data: {nickname: nickname},

        success: function(response) {
            if (response) {
                if (confirm("사용 가능한 닉네임입니다. 사용하시겠습니까?")) {
                    // 사용 버튼을 클릭한 경우
                    $("#nickname").prop("readonly", true);
                    $("#duplicateCheckButton").hide();
                    $("#changeButton").show();
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
    if (!duplicateChecked) {
        alert("아이디 중복 확인을 완료해 주세요.");
        return false;
    }

    if(!duplicateNicknameChecked)
    {
         alert("닉네임 중복 확인을 완료해 주세요.");
         return false;
    }
    return true;
}

function changeuserId() {
    $("#userId").prop("readonly", false);
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

