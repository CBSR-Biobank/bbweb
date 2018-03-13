/**
 * AngularJS component available to the rest of the application.
 *
 * @namespace common.components.tagsList
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

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
 * An AngularJS component that displays a list of strings using Bootstrap tags.
 *
 * @memberOf common.components.tagsList
 *
 * @param {Array<common.components.tagsList.TagInfo>} tagData
 *
 * @param {string} labelClass - the class to use to display the strings. The following can be used:
 *        `label-default`, `label-primary`, `label-success`, `label-info`, `label-warning`, or `label-danger`.
 *        If no class is specified, then `label-primary` is used.
 *
 * @param {common.components.tagsList.onTagSelected} onTagSelected
 */
const tagsListComponent = {
  template: require('./tagsList.html'),
  controller: TagsListController,
  controllerAs: 'vm',
  bindings: {
    tagData:       '<',
    tagClass:      '@',
    onTagSelected: '&'
  }
};

/**
 * Used by {@link common.components.tagsList.tagsListComponent tagsListComponent} to store tag information.
 *
 * @typedef common.components.tagsList.TagInfo
 * @type object
 *
 * @property {String} label - the function that returns the translated string to display for the tag.
 *
 * @property {string} tooltip - the function that returns the translated text to display for the tag's tooltip.
 *
 * @property {object} obj - the object to return when a tag is selected.
 */

/**
 * The callback function called by {@link common.components.tagsList.tagsListComponent tagsListComponent}
 * when the user selects one of the tags.
 *
 * @callback common.components.tagsList.onTagSelected
 *
 * @param {object} obj - the `obj` field of the object selected (see {@link
 * common.components.tagsList.TagInfo TagInfo}).
 *
 * @returns {undefined}
 */

export default ngModule => ngModule.component('tagsList', tagsListComponent)
