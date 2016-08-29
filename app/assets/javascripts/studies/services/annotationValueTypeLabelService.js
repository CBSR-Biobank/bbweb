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
   * Description
   */
  function annotationValueTypeLabelService(gettextCatalog, AnnotationValueType) {
    var labels = {};

    /// annotation value type
    labels[AnnotationValueType.TEXT] = gettextCatalog.getString('Text');

    /// annotation value type
    labels[AnnotationValueType.NUMBER]  = gettextCatalog.getString('Number');

    /// annotation value type
    labels[AnnotationValueType.DATE_TIME]  = gettextCatalog.getString('Date and time');

    /// annotation value type
    labels[AnnotationValueType.SELECT]  = gettextCatalog.getString('Select');

    var service = {
      valueTypeToLabel: valueTypeToLabel
    };
    return service;

    //-------

    function valueTypeToLabel(vt) {
      var result = labels[vt];
      if (_.isUndefined(result)) {
        throw new Error('invalid annotation value type: ' + vt);
      }
      return result;
    }

  }

  return annotationValueTypeLabelService;
});
