/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  annotationValueTypeLabelService.$inject = [
    'gettextCatalog',
    'AnnotationValueType'
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
  function annotationValueTypeLabelService(gettextCatalog, AnnotationValueType) {
    var labels = {};

    labels[AnnotationValueType.TEXT]      = gettextCatalog.getString('Text');
    labels[AnnotationValueType.NUMBER]    = gettextCatalog.getString('Number');
    labels[AnnotationValueType.DATE_TIME] = gettextCatalog.getString('Date and time');
    labels[AnnotationValueType.SELECT]    = gettextCatalog.getString('Select');

    var service = {
      valueTypeToLabel: valueTypeToLabel
    };
    return service;

    //-------

    function valueTypeToLabel(valueType, isSingleSelect) {
      var result;

      if (valueType === AnnotationValueType.SELECT) {
        isSingleSelect = _.isUndefined(isSingleSelect) ? true : isSingleSelect;
        if (isSingleSelect) {
          return gettextCatalog.getString('Single Select');
        }
        return gettextCatalog.getString('Multiple Select');
      }

      result = labels[valueType];
      if (_.isUndefined(result)) {
        throw new Error('invalid annotation value type: ' + valueType);
      }
      return result;
    }

  }

  return annotationValueTypeLabelService;
});
