//
// This script is used by the Collection Event Annotation Type add / update form. It provides the form
// with a way to add select items.
//

var selectionGroup = $('.selectionGroup');

var selectionsToggle = function() {
  if ($('#valueType').val() == "Select") {
    $('.selectionGroup').show();
  } else {
    $('.selectionGroup').hide();
  }
};

selectionsToggle();

//
// Shows or hides the selection group based on the value type selected
//
$('#valueType').change(function() {
  selectionsToggle();
});

//
// creates a new selection text entry field based on the hidden template. The
// new input field has to be modified to contain the correct parameters
//
$(document).on('click', '.addSelection', function() {
  var template = $('.selection_template', selectionGroup);
  var newSelection = template.clone().removeClass('selection_template').addClass('selections');
  var i = $('.selectionGroup .selections').size();
  newSelection.find('input').attr('name', "selections[" + i + "]");
  newSelection.show();
  template.before(newSelection);
});

//
// removes a selection text entry field based on the button pressed
//
$(document).on('click', '.removeSelection', function() {
  var i = $('.selectionGroup .selections').size();
  if (i > 1) {
    $(this).parent().parent().remove();
    renumber();
  }
});

// -- renumber fields
//
// Rename fields to have a coherent payload like:
//
// selections[0]
// selections[1]
//
var renumber = function() {
  selectionGroup.each(function(i) {
    $('.selections input', this).each(function(i) {
      var name = $(this).attr('name').replace(/selections\[.+\]/g, "selections[" + i + "]");
      $(this).attr('name', name);
    });
  });
};
