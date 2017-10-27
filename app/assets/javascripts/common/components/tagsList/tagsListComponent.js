/**
 *
 */

import _ from 'lodash'

/**
 * @typedef common.components.tagsList.TagsInfo
 * @type object
 *
 * @property {String} label - the label to display to the user
 *
 * @property {string} tooltip - the tooltip text to display for the label.
 *
 * @property {object} obj - the object to return when a label is selected.
 */

const DefaultLabelClass = 'label-info';

/*
 *
 */
class TagsListController {

  $onInit() {
    if (_.isUndefined(this.tagClass)) {
      this.tagClass = DefaultLabelClass;
    }
  }

  tagSelected(tagInfo) {
    if (this.onTagSelected()) {
      this.onTagSelected()(tagInfo.obj);
    }
  }

}

/**
 * Displays a list of strings using Bootstrap tags.
 *
 * @param {Array<common.components.tagsList.TagsInfo>} tagData - the label data.
 *
 * @param {string} labelClass - the class to use to display the strings. The following can be used:
 *        label-default, label-primary, label-success, label-info, label-warning, or label-danger. If no
 *        class is specified, then 'label-primary' is used.
 *
 * @param {function} onTagselected - the function to invoke when a label is selected.
 */
const component = {
  template: require('./tagsList.html'),
  controller: TagsListController,
  controllerAs: 'vm',
  bindings: {
    tagData:       '<',
    tagClass:      '@',
    onTagSelected: '&'
  }
};

export default ngModule => ngModule.component('tagsList', component)
