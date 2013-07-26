//
// This script is used by the Collection Event Type add / update form. It provides the form
// with a way to add specimen groups.
//
// sgUnits is defined in the page that includes this script.
//
var sgTemplate = $('.sg_data_template')
var atTemplate = $('.at_data_template')

var sgRenameElementAttr = function(element, id) {
    var namerepl = element.attr('name').replace(/specimenGroupData\[.+\]/g, "specimenGroupData[" + id + "]")
    element.attr('name', namerepl)
}

var addOnUpdate = function(event) {
    // updates the "units" on the amount field
    var addOn = $(this).parent().parent().parent().find('.add-on')
    addOn.text(sgUnits[$(this).val()])
}

// update existing amount fields "units"
$('.add-on').each(function(i,v) {
    var select = $(this).parent().parent().parent().parent().find('select')
    $(this).text(sgUnits[select.val()])
})

    // bind function to existing sg selects
    $('select[id*="specimenGroupData"]').each(function(i,v) {
        $(this).bind('change', addOnUpdate)
    })

        var sgRenameGroup = function(sg, id) {
            sgRenameElementAttr(sg.find('select'), id)
            sg.find('input').each(function() {
                sgRenameElementAttr($(this), id)
            })

                var select = sg.find('select')
            select.bind('change', addOnUpdate)
            //var selectLabel = select.parent().parent().find('label')
            //var newText =  selectLabel.text().replace(/[0-9]+/g, (id + 1))
            //selectLabel.text(newText)
        }

//
// creates a new selection text entry field based on the hidden template. The
// new input field has to be modified to contain the correct parameters
//
$(document).on('click', '.addSpecimenGroup', function() {
    var i = $('.sg_data').size()
    var newSg = sgTemplate.clone().removeClass('sg_data_template').addClass('sg_data')
    sgRenameGroup(newSg, i)
    newSg.show()
    sgTemplate.before(newSg)
})

//
// removes a selection text entry field based on the button pressed
//
$(document).on('click', '.removeSpecimenGroup', function() {
    var i = $('.sg_data').size()
    if (i > 0) {
        $(this).parent().parent().parent().remove()
        renumberSpecimenGroups()
    }
})

// -- renumber fields specimen groups
//
var renumberSpecimenGroups = function() {
    $('.sg_data').each(function(i) {
        sgRenameGroup($(this), i)
    })
        }
