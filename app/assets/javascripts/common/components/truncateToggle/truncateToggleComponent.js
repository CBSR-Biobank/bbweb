/**
 * AngularJS Component available to the rest of the application.
 *
 * @namespace common.components.truncateToggle
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

class TruncateToggleController {

  constructor($filter, gettextCatalog) {
    'ngInject';
    Object.assign(this, { $filter, gettextCatalog });

    this.showLessLabel = gettextCatalog.getString('Show less');
    this.showMoreLabel = gettextCatalog.getString('Show more');

    this.toggleState = false;
    this.buttonLabel = this.showLessLabel;
  }

  $onInit() {
    this.text = this.text || '';
    this.determineDisplayText();
  }

  $onChanges(changed) {
    if (changed.text) {
      this.text = changed.text.currentValue || '';
      this.toggleRequired = (this.text.length > this.toggleLength);
      this.determineDisplayText();
    }
  }

  toggleText() {
    this.toggleState = !this.toggleState;
    this.determineDisplayText();
    this.buttonLabel = this.toggleState ? this.showMoreLabel : this.showLessLabel;
  }

  determineDisplayText() {
    this.displayText = this.toggleState ?
      this.$filter('truncate')(this.text, this.toggleLength) : this.text;
  }
}

/**
 * An AngularJS Component meant for displaying lengthy text that also allows the user to truncate it by
 * pressing a `Toggle` button.
 *
 * The component watches out for changes to the text and updates it accordingly.
 *
 * @memberOf common.components.truncateToggle
 *
 * @param {string} text - the text to display.
 *
 * @param {int} toggleLength - the size of the string after is it truncated.
 *
 * @param {string} textEmptyWarning - the string to display if `text` is an empty string.
 *
 * @param {boolean} allowEdit - when `TRUE` a button is also displayed that allows the user to edit the text.
 *
 * @param {function} onEdit - the function that is called when the presses the `Edit` button.
 *
 * @param {string} tooltip - the text to display when the user hovers over the `Toggle` button.
 */
const truncateToggleComponent = {
  template: require('./truncateToggle.html'),
  controller: TruncateToggleController,
  controllerAs: 'vm',
  bindings: {
    text:             '<',
    toggleLength:     '<',
    textEmptyWarning: '@',
    allowEdit:        '<',
    onEdit:           '&',
    tooltip:          '@'
  }
};

export default ngModule => ngModule.component('truncateToggle', truncateToggleComponent)
