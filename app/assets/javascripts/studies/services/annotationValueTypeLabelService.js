/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function (_) {
  'use strict';

  annotationValueTypeLabelService.$inject = [
    'gettext',
    'AnnotationValueType'
  ];

  /**
   * Description
   */
  function annotationValueTypeLabelService(gettext, AnnotationValueType) {
    var labels = {};

    /// annotation value type
    labels[AnnotationValueType.TEXT] = gettext('Text');

    /// annotation value type
    labels[AnnotationValueType.NUMBER]  = gettext('Number');

    /// annotation value type
    labels[AnnotationValueType.DATE_TIME]  = gettext('Date and time');

    /// annotation value type
    labels[AnnotationValueType.SELECT]  = gettext('Select');

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
