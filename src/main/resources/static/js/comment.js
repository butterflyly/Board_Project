
// 댓글 작성 메서드
const CommentSave = () => {

 // const content = document.getElementById("content").value.trim();
//  const secret = document.getElementById("secret").checked ? true : false;

   let formData = new FormData();
   formData.append("content", document.getElementById("content").value.trim());
   formData.append("secret" , document.getElementById("secret").checked ? true : false);
   formData.append("file", document.getElementById("comment-file").files[0]); // File객체 담기

   var token = $("meta[name='_csrf']").attr("content");
   var header = $("meta[name='_csrf_header']").attr("content");
   let boardId = $("#boardId").val();

   const content = formData.get("content"); // 댓글 내용

   if (content.length == 0) {
     alert('답글을 입력해주세요');
     return;
   }

   $.ajax({
     // 요청방식: post, 요청주소: /comment/create/question
     // 요청데이터: 작성내용, 게시글번호, 비밀 댓글 여부, 부모 댓글 id
     beforeSend: function (xhr) {
            xhr.setRequestHeader(header, token);
          },
     type: "post",
     url: `/comment/create/${boardId}`,
     data: formData,
  //   data: {
  //     "content" : content,
  //     "secret" : secret
  //   },
     processData: false, // processData : false 선언시 formData를 string으로 변환하지 않음.
     contentType: false, // contentType : false 선언시 multipart/form-data로 전송되게 하는 옵션.
     success: function (fragment) {
         alert("댓글이 작성되었습니다!!🎉");
         location.href = `/board/detail/${boardId}`;
     },
     error: function (err) {
       console.log("요청 실패", err);
     }
   });
}



// 답변, 댓글 수정 동시에 못하도록 하기 위한 답변 폼 숨기기 메서드
const hideReplyForm = (commentIndex) => {
     const formId = "reply-form-" + commentIndex;
     const formElement = document.getElementById(formId);
     if (formElement) {
        formElement.classList.add("visually-hidden");
     } else {
         console.error("Element with ID '" + formId + "' not found.");
     }
};

// 답변, 댓글 수정 동시에 못하도록 하기 위한 수정 폼 숨기기 메서드
const hideModifyForm = (commentIndex) => {
  const formId = "modify-form-" + commentIndex;
  const formElement = document.getElementById(formId);
  if (formElement) {
    formElement.classList.add("visually-hidden");
  } else {
    console.error("Element with ID '" + formId + "' not found.");
  }
};

// 답글 버튼 눌렀을 때 입력 창 나오게
const showReplyForm = (commentIndex) => {
  console.log("showReplyForm 호출");
  const formId = "reply-form-" + commentIndex;
  const formElement = document.getElementById(formId);
  if (formElement) {
    formElement.classList.remove("visually-hidden");
    hideModifyForm(commentIndex); // 수정 입력 폼 숨기기
  } else {
    console.error("Element with ID '" + formId + "' not found.");
  }
};

// 댓글 수정 버튼 눌렀을 때 입력 창 나오게
const showModifyForm = (commentIndex) => {
  console.log("showModifyForm 호출");
  const formId = "modify-form-" + commentIndex;
  // 기존 값 가져오기 필요
  const formElement = document.getElementById(formId);
  const existingCommentContents = document.getElementById("comment-content-" + commentIndex).value;
  const existingSecretValue = document.getElementById("comment-secret-" + commentIndex).value;

  if (formElement) {
    hideReplyForm(commentIndex); // 답글 입력 폼 숨기기
    formElement.classList.remove("visually-hidden");
    // 기존 값 채우기
    document.getElementById("modify-comment-contents-" + commentIndex).value = existingCommentContents;
    document.getElementById("modify-secret-" + commentIndex).checked = (existingSecretValue == "true");
    // ...
  } else {
    console.error("Element with ID '" + formId + "' not found.");
  }
}


// 답글(대댓글) 수정 버튼 눌렀을 때 입력 창 나오게
const showReplyModifyForm = (childIndex) => {
  console.log("showReplyModifyForm 호출");
  const formId = "modify-reply-form-" + childIndex;
  const formElement = document.getElementById(formId);
  const existingCommentContents = document.getElementById("child-comment-content-" + childIndex).value;
  const existingSecretValue = document.getElementById("child-comment-secret-" + childIndex).value;

  if (formElement) {
    formElement.classList.remove("visually-hidden");
    // 기존 값 채우기
    document.getElementById("modify-reply-comment-contents-" + childIndex).value = existingCommentContents;
    document.getElementById("modify-reply-secret-" + childIndex).checked = (existingSecretValue == "true");
    // ...
  } else {
    console.error("Element with ID '" + formId + "' not found.");
  }

}

// 댓글 수정 메서드
const modifyCommentWrite = (commentIndex) => {

     let formData = new FormData();
     formData.append("content", document.getElementById("modify-comment-contents-" + commentIndex).value.trim());
     formData.append("secret" ,document.getElementById("modify-secret-" + commentIndex).checked ? true : false);
     formData.append("commentUsers", document.getElementById("modify-writer-" + commentIndex).value); // File객체 담기
     formData.append("pageNumber" , $('input[name=boardCommentPaging_number]').val());
     formData.append("commentId", document.getElementById("modify-comment-" + commentIndex).value);
     formData.append("comment-update-file", document.getElementById("modify-file"+commentIndex).files[0]); // File객체 담기

     var token = $("meta[name='_csrf']").attr("content");
     var header = $("meta[name='_csrf_header']").attr("content");
     let boardId = $("#boardId").val();
     const commentId = formData.get("commentId")

     const content = formData.get("content");

    if (content.length == 0) {
      alert('답글을 입력해주세요');
      return;
    }

    var pageNumber = formData.get("pageNumber");


    $.ajax({
    // 요청방식: post, 요청주소: /comment/reply/create
    // 요청데이터: 작성내용, 게시글번호, 비밀 댓글 여부, 부모 댓글 id
    type: "post",
    url: `/comment/update/${commentId}`,
    beforeSend: function (xhr) {
              xhr.setRequestHeader(header, token);
            },
    data: formData,
    processData: false, // processData : false 선언시 formData를 string으로 변환하지 않음.
    contentType: false, // contentType : false 선언시 multipart/form-data로 전송되게 하는 옵션.

    success: function (data) {
        if(data ==='UpdateSuccess') {
    	             console.log("댓글 내용 :" + content);
                      alert("댓글이 수정되었습니다!!🎉");
                      location.href = `/board/detail/${boardId}?page=${pageNumber}`;
                      console.log("댓글 내용 :" + content);
    	       } else {
    	       alert('댓글 수정에 실패했습니다.');
    	       }
    },
    error: function (err) {
      console.log("요청 실패", err);
    }
    });
}

// 답글(대댓글) 수정 메서드
const modifyReplyCommentWrite = (childIndex) => {

    let formData = new FormData();
        formData.append("content", document.getElementById("modify-reply-comment-contents-" + childIndex).value.trim());
        formData.append("secretValue" , document.getElementById("modify-reply-secret-" + childIndex).checked ? true : false);
        formData.append("commentUsers", document.getElementById("modify-reply-writer-" + childIndex).value); // File객체 담기
        formData.append("pageNumber" , $('input[name=boardCommentPaging_number]').val());
        formData.append("commentId", document.getElementById("modify-reply-comment-" + childIndex).value);
        formData.append("comment-update-file", document.getElementById("reply-modify-file"+ childIndex).files[0]); // File객체 담기


        let boardId = $("#boardId").val();

        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");
        const commentId = formData.get("commentId")
        const content = formData.get("content");

        if (content.length == 0) {
             alert('대댓글을 입력해주세요');
             return;
         }

       var pageNumber = formData.get("pageNumber");


       $.ajax({
       // 요청방식: post, 요청주소: /comment/reply/create
       // 요청데이터: 작성내용, 게시글번호, 비밀 댓글 여부, 부모 댓글 id
       type: "post",
       url: `/comment/update/${commentId}`,
       beforeSend: function (xhr) {
                 xhr.setRequestHeader(header, token);
               },
       data: formData,
       processData: false, // processData : false 선언시 formData를 string으로 변환하지 않음.
       contentType: false, // contentType : false 선언시 multipart/form-data로 전송되게 하는 옵션.

       success: function (data) {
           if(data ==='UpdateSuccess') {
       	             console.log("댓글 내용 :" + content);
                         alert("댓글이 수정되었습니다!!🎉");
                         location.href = `/board/detail/${boardId}?page=${pageNumber}`;
                         console.log("댓글 내용 :" + content);
       	       } else {
       	       alert('댓글 수정에 실패했습니다.');
       	       }
       },
       error: function (err) {
         console.log("요청 실패", err);
       }
       });

}


// 답글(대댓글) 작성 메서드
const replyCommentWrite = (commentIndex) => {

   let formData = new FormData();
   formData.append("content", document.getElementById("replyCommentContents-" + commentIndex).value.trim());
   formData.append("secret" , document.getElementById("replySecret-" + commentIndex).checked ? true : false);
   formData.append("reply-comment-file", document.getElementById("reply-comment-file"+commentIndex).files[0]); // File객체 담기
   formData.append("parentId" ,document.getElementById("comment-" + commentIndex).value);
   formData.append("pageNumber" , $('input[name=boardCommentPaging_number]').val());

   var token = $("meta[name='_csrf']").attr("content");
   var header = $("meta[name='_csrf_header']").attr("content");
   let boardId = $("#boardId").val();

   const content = formData.get("content");

  if (content.length == 0) {
    alert('답글을 입력해주세요');
    return;
  }

  var pageNumber = formData.get("pageNumber");


  $.ajax({
    // 요청방식: post, 요청주소: /comment/reply/create
    // 요청데이터: 작성내용, 게시글번호, 비밀 댓글 여부, 부모 댓글 id
    type: "post",
    url: `/comment/reply/create`,
     beforeSend: function (xhr) {
              xhr.setRequestHeader(header, token);
            },
    data: formData,
    processData: false, // processData : false 선언시 formData를 string으로 변환하지 않음.
    contentType: false, // contentType : false 선언시 multipart/form-data로 전송되게 하는 옵션.
    success: function (fragment) {
            if(fragment ==='Success') {
        	             alert("대댓글이 작성되었습니다!!🎉 ");
                         location.href = `/board/detail/${boardId}?page=${pageNumber}`;
        	       } else {
        	       alert('댓글을 작성할 수 없거나 권한이 없습니다.');
        	       }
    },
    error: function (err) {
      console.log("요청 실패", err);
    }
  });
}



// 댓글 삭제 메서드
const deleteComment = (commentIndex) => {
  let boardId = $("#boardId").val();
  const commentId = document.getElementById("comment-" + commentIndex).value; // 댓글번호
  const writerId = document.getElementById("writer-" + commentIndex).value; // 작성자 ID
 // const currentPage = [[${questionCommentPaging.number}]];
  var header = $("meta[name='_csrf_header']").attr('content');
  var token = $("meta[name='_csrf']").attr('content');



  $.ajax({
    // 요청방식: post, 요청주소: /comment/reply/create
    // 요청데이터: 작성내용, 게시글번호, 비밀 댓글 여부, 부모 댓글 id
    type: "post",
    url: `/comment/delete/${commentId}`,
         beforeSend: function (xhr) {
              xhr.setRequestHeader(header, token);
            },
    data: {
      "boardId": boardId,
      "commentUsers": writerId,
      "id": commentId,
  //    "page": currentPage
    },
    success: function (fragment) {
          alert("댓글이 삭제되었습니다.!!🎉 ");
          location.href = `/board/detail/${boardId}`;
    },
    error: function (err) {
      console.log("요청 실패", err);
    }
  });
}

//답글(대댓글) 삭제 메서드
const deleteReplyComment = (childIndex) => {
  const writerId = document.getElementById("modify-reply-writer-" + childIndex).value; // 작성자 ID
  const commentId = document.getElementById("modify-reply-comment-" + childIndex).value; // 댓글번호


  let boardId = $("#boardId").val();
  var header = $("meta[name='_csrf_header']").attr('content');
  var token = $("meta[name='_csrf']").attr('content');

  $.ajax({
    // 요청방식: post, 요청주소: /comment/delete
    // 요청데이터: 작성내용, 게시글번호, 비밀 댓글 여부, 부모 댓글 id
    type: "post",
    url: `/comment/delete/${commentId}`,
    beforeSend: function (xhr) {
      xhr.setRequestHeader(header, token);
    },
    data: {
        "boardId": boardId,
        "commentUsers": writerId,
        "id": commentId,
    },
    success: function (fragment) {
         alert("댓글이 삭제되었습니다.!!🎉 ");
         location.href = `/board/detail/${boardId}`;
    },
    error: function (err) {
      console.log("요청 실패", err);
    }
  });
}

// 테스트
const deleteItem = (itemId) =>
{
      // 버튼 요소 가져오기
      const button = document.getElementById('deleteButton-' + itemId);

      // 버튼 삭제
      button.parentNode.removeChild(button);
}

const fileDelete = (fileIndex) => {

     const commentId = document.getElementById("modify-file-comment-" + fileIndex).value; // 댓글번호
     const imageId = document.getElementById("modify-file-" + fileIndex).value; // 이미지번호
     const button = document.getElementById('file-delete-' + fileIndex);
     const Replybutton = document.getElementById('reply-file-delete-' + fileIndex);

     var header = $("meta[name='_csrf_header']").attr('content');
     var token = $("meta[name='_csrf']").attr('content');

     $.ajax({
        // 요청방식: post, 요청주소: /comment/delete
        // 요청데이터: 작성내용, 게시글번호, 비밀 댓글 여부, 부모 댓글 id
        type: "post",
        url: `/comment/Image/delete/${imageId}/${commentId}`,
        beforeSend: function (xhr) {
          xhr.setRequestHeader(header, token);
        },
        data: {
            "imageId": imageId,
            "commentId": commentId,
        },
        success: function (fragment) {
             alert("파일이 삭제되었습니다.!!🎉");
            console.log(fileIndex);

            if(button)
            {
                button.remove();
            }
            if(Replybutton)
            {
                Replybutton.remove();
            }
        },
        error: function (err) {
          console.log("요청 실패", err);
        }
      });
}
