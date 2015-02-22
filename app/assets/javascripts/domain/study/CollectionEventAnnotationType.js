define(['../module'], function(module) {
  'use strict';

  module.factory('CollectionEventAnnotationType', CollectionEventAnnotationTypeFactory);

  CollectionEventAnnotationTypeFactory.$inject = ['StudyAnnotationType', 'AnnotationValueType'];

  /**
   *
   */
  function CollectionEventAnnotationTypeFactory(StudyAnnotationType, AnnotationValueType) {

    function CollectionEventAnnotationType(obj) {
      obj = obj || {};
      StudyAnnotationType.call(this, obj);
    }

    CollectionEventAnnotationType.prototype = Object.create(StudyAnnotationType.prototype);

    return CollectionEventAnnotationType;
  }

});
