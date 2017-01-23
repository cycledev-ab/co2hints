var givenAnswers = "";

$(document).ready(function() {
    $.ajax({
        url: "api",
        dataType: "json",
        success: function (data) {
            $.each(data, function(index, question) {
                $.get('question.mst.html', function(template) {
                  var rendered = Mustache.render(template, {question: question});
                  $('#questions').append(rendered);
                });

            });

        },
        error: function (result) {
            }
    });
});