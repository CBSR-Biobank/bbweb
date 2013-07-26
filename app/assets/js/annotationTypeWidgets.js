//
// This script is used by the Collection Event Type add / update form. It provides the form
// with a way to add specimen groups.
//

// annotation type rename
var atRenameElementAttr = function(element, id) {
    var namerepl = element.attr('name').replace(/annotationTypeData\[.+\]/g, "annotationTypeData[" + id + "]")
    element.attr('name', namerepl)
}

var atRenameGroup = function(sg, id) {
    atRenameElementAttr(sg.find('select'), id)
    sg.find('input').each(function() {
        atRenameElementAttr($(this), id)
    })
        }

//
// creates a new selection text entry field based on the hidden template. The
// new input field has to be modified to contain the correct parameters
//
$(document).on('click', '.addAnnotationType', function() {
    var i = $('.at_data').size()
    var newAt = atTemplate.clone().removeClass('at_data_template').addClass('at_data')
    atRenameGroup(newAt, i)
    newAt.show()
    atTemplate.before(newAt)
})

//
// removes a selection text entry field based on the button pressed
//
$(document).on('click', '.removeAnnotationType', function() {
    var i = $('.at_data').size()
    if (i > 0) {
        $(this).parent().parent().parent().remove()
        renumberAnnotationTypes()
    }
})

// -- renumber fields specimen groups
//
var renumberAnnotationTypes = function() {
    $('.at_data').each(function(i) {
        atRenameGroup($(this), i)
    })
        }
