/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash';

/**
 * An AngularJS service that converts an annotation "value type" to a i18n string that can
 * be displayed to the user.
 *
 * @param {object} gettextCatalog - The service that allows strings to be translated to other languages.
 *
 * @param {object} AnnotationValueType - AngularJS constant that enumerates all the annotation value types.
 *
 * @return {Service} The AngularJS service.
 */
/* @ngInject */
function annotationValueTypeLabelService(gettextCatalog, AnnotationValueType, labelService) {
  var labels = {};

  labels[AnnotationValueType.TEXT]      = () => gettextCatalog.getString('Text');
  labels[AnnotationValueType.NUMBER]    = () => gettextCatalog.getString('Number');
  labels[AnnotationValueType.DATE_TIME] = () => gettextCatalog.getString('Date and time');
  labels[AnnotationValueType.SELECT]    = () => gettextCatalog.getString('Select');

  var service = {
    valueTypeToLabelFunc,
    getLabels
  };
  return service;

  //-------

  function valueTypeToLabelFunc(valueType, isSingleSelect) {
    if (valueType === AnnotationValueType.SELECT && !_.isUndefined(isSingleSelect)) {
      if (isSingleSelect) {
        return function () { return gettextCatalog.getString('Single Select'); };
      } else {
        return function () { return gettextCatalog.getString('Multiple Select'); };
      }
    }

    return labelService.getLabel(labels, valueType);
  }

  function getLabels() {
    return Object.values(AnnotationValueType).map((valueType) => ({
      id:        valueType,
      labelFunc: valueTypeToLabelFunc(valueType)
    }));
  }

}

export default ngModule => ngModule.service('annotationValueTypeLabelService',
                                           annotationValueTypeLabelService)
