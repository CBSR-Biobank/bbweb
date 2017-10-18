/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  annotationValueTypeLabelService.$inject = [
    'gettextCatalog',
    'AnnotationValueType',
    'labelService'
  ];

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
  function annotationValueTypeLabelService(gettextCatalog, AnnotationValueType, labelService) {
    var labels = {};

    labels[AnnotationValueType.TEXT]      = function () { return gettextCatalog.getString('Text'); };
    labels[AnnotationValueType.NUMBER]    = function () { return gettextCatalog.getString('Number'); };
    labels[AnnotationValueType.DATE_TIME] = function () { return gettextCatalog.getString('Date and time'); };
    labels[AnnotationValueType.SELECT]    = function () { return gettextCatalog.getString('Select'); };

    var service = {
      valueTypeToLabelFunc: valueTypeToLabelFunc,
      getLabels:            getLabels
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
      return _.values(AnnotationValueType).map((valueType) => ({
        id:        valueType,
        labelFunc: valueTypeToLabelFunc(valueType)
      }));
    }

  }

  return annotationValueTypeLabelService;
});
