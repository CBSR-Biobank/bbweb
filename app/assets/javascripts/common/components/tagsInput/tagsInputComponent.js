/**
 * AngularJS component available to the rest of the application.
 *
 * @namespace common.components.tagsInput
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

const DefaultLabel = 'label-info'

/*
 *
 */
class TagsInputController {

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
 * A component that allows the user to input tags, coming from callback {@link
 * common.components.tagsInput.onGetValues onGetValues}, into an INPUT field in an HTML Form.
 *
 * The tags are displayed below the input field. Tags can also be removed from the display area.
 *
 * @memberOf common.components.tagsInput
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
 * @param {common.components.tagsInput.onGetvalues} onGetValues
 *
 * @param {common.components.tagsInput.onTagSelected} onTagSelected
 *
 * @param {common.components.tagsInput.onTagRemoved} onTagRemoved
 *
 * @param {boolean} required - when `TRUE` the form cannot be submitted if the user has not selected any tags.
 */
const tagsInputComponent = {
  template: require('./tagsInput.html'),
  controller: TagsInputController,
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
};

/**
 * The callback function called by {@link common.components.tagsInput.tagsInputComponent tagsInputComponent}
 * to retrieve the matching tag values after the user enters a value into the INPUT field.
 *
 * @callback common.components.tagsInput.onGetValues
 *
 * @param {string} viewValue - the text  entered by the user.
 *
 * @returns {Array<common.components.tagsInput.MatchingTags>}
 */

/**
 * The callback function called by {@link common.components.tagsInput.tagsInputComponent tagsInputComponent}
 * when the user selects one of the tags from the list of available options.
 *
 * @callback common.components.tagsInput.onTagSelected
 *
 * @param {object} obj - the `obj` field of the object selected (see {@link
 * common.components.tagsInput.onGetValues onGetValues} and {@link common.components.tagsInput.MatchingTags
 * MatchingTags}).
 *
 * @returns {undefined}
 */

/**
 * The callback function called by {@link common.components.tagsInput.tagsInputComponent tagsInputComponent}
 * when the user clicks on a tag in the tags display area (usually this callback removes the tag)
 *
 * @callback common.components.tagsInput.onTagRemoved
 *
 * @param {object} obj - the `obj` field of the object selected (see {@link
 * common.components.tagsInput.onGetValues onGetValues} and {@link common.components.tagsInput.MatchingTags
 * MatchingTags}).
 *
 * @returns {undefined}
 */

/**
 * A *Tag Item* displayed by {@link common.components.tagsInput.tagsInputComponent tagsInputComponent}.
 *
 * @typedef common.components.tagsInput.MatchingTags
 *
 * @type object
 *
 * @property {string} label - the function that returns the translated string to display on the page.
 *
 * @property {object} obj - the object to be returned when a tag is selected by the user.
 */

export default ngModule => ngModule.component('tagsInput', tagsInputComponent)
