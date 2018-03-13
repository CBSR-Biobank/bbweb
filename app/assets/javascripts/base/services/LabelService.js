/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'
import angular from 'angular'

/**
 * An AngualrJS service that is used to map IDs to text labels.
 *
 * @memberOf base.services
 */
class LabelService {

  /**
   * @param {base.BbwebError} BbwebError - AngularJS factory for exceptions.
   *
   * @param {Array<base.services.LabelService.LabelInfo>} labelData=[]
   */
  constructor(BbwebError, labelData = []) {
    Object.assign(this, { BbwebError });
    this.setLabels(labelData);
  }

  /**
   *
   * @protected
   *
   */
  setLabels(labelData) {
    this.labels = {};
    labelData.forEach(labelInfo => {
      this.labels[labelInfo.id] = labelInfo.label;
    });
  }

  /**
   * Allows for converting enumerations to labels that are displayed on a web page.
   *
   * @protected
   *
   * @param {string} value - the enumerated type value to return the label function for.
   *
   * @return {function} a function that returns a translated string.
   *
   * @throws {base.BbwebError} if the value is not a key in the `labels` object.
   *
   * @see  admin.common.services.AnnotationValueTypeLabelService
   */
  getLabel(value) {
    var result = this.labels[value];
    if (_.isUndefined(result)) {
      throw new this.BbwebError('no such label: ' + value);
    }
    return result;
  }

}

/**
 * Used to map IDs to functions that return label text translated to the currently selected language.
 *
 * @typedef base.services.LabelService.LabelInfo
 *
 * @type object
 *
 * @property {string} id - the ID to associate with the label.
 *
 * @property {function} label - a function that returns the label text in the selected language.
 */


export { LabelService }

// this service does not need to be included in AngularJS since it is imported by the services that
// extend it
export default () => angular.noop
