/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

const DefaultLabel = 'label-info'

/*
 *
 */
class Controller {

  constructor($scope) {
    'ngInject'

    Object.assign(this, { $scope })
  }

  $onInit() {
    this.tags = []
    this.labelClass = DefaultLabel
  }

  getValues(viewValue) {
    return this.onGetValues()(viewValue);
  }

  tagSelected(value) {
    this.value = ''
    this.tags.push(value)
    this.onTagSelected()(value.obj)
    if (this.required) {
      this.$scope.tagsForm.tagsInput.$setValidity('tagsEntered', true)
    }
  }

  tagRemoved(tagToRemove) {
    const found = this.tags.find((el) => el.label === tagToRemove.label)
    if (!found) {
      throw new Error('tag was never selected: ' + tagToRemove.label)
    }
    _.remove(this.tags, function (tag) {
      return tag.label === tagToRemove.label
    })
    this.onTagRemoved()(tagToRemove.obj)

    if (this.required && (this.tags.length <= 0)) {
      this.$scope.tagsForm.tagsInput.$setValidity('tagsEntered', false)
    }
  }

}

/**
 * A component that allows the user to input tags coming from callback {@link onGetValues}.
 *
 * The tags are displayed below the input field. Tags can also be removed from the display area.
 *
 * @param {string} label - the label to display in the form containing this component.
 *
 * @param {string} placeholder - the placeholder text to display in the input when it is empty.
 *
 * @param {string} noResultsFound - the text to display when onGetValues() returns null.
 *
 * @param {string} noTagsErrorMessage - the error text to display when this tags are required and the user
 *        has removed all the tags.
 *
 * @param {function} onGetValues - the function to call that returns a list of available tag values. This
 *        function takes one parameter: the value entered by the user. The return value should be an array of
 *        objects where each object has two fields: 'label' a function to call to display the value, and 'obj'
 *        the object to be returned by 'onTagSelected' when the value is selected by the user.
 *
 * @param {function} onTagSelected - the function to call when the user selects one of the tags from the list
 *        of available options. This function has one parameter, the 'obj' field of the object selected (see
 *        onGetValues).
 *
 * @param {function} onTagRemoved - the function to call when the user clicks on a tag in the tags display
 *        area (usually this callback removes the tag). This function has one parameter, the 'obj' field of
 *        the object selected (see onGetValues).
 */
const COMPONENT = {
  template: require('./tagsInput.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
    label:              '@',
    placeholder:        '@',
    tagsPlaceholder:    '@',
    noResultsFound:     '@',
    noTagsErrorMessage: '@',
    onGetValues:        '&',
    onTagSelected:      '&',
    onTagRemoved:       '&',
    required:           '<'
  }
}


export default ngModule => ngModule.component('tagsInput', COMPONENT)
