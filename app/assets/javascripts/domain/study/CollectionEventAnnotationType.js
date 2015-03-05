define(['../module'], function(module) {
  'use strict';

  module.factory('CollectionEventAnnotationType', CollectionEventAnnotationTypeFactory);

  CollectionEventAnnotationTypeFactory.$inject = [
    'StudyAnnotationType',
    'AnnotationValueType',
    'ceventAnnotTypesService'
  ];

  /**
   *
   */
  function CollectionEventAnnotationTypeFactory(StudyAnnotationType,
                                                AnnotationValueType,
                                                ceventAnnotTypesService) {

    function CollectionEventAnnotationType(obj) {
      obj = obj || {};
      StudyAnnotationType.call(this, obj);
      this._service = ceventAnnotTypesService;
    }

    CollectionEventAnnotationType.prototype = Object.create(StudyAnnotationType.prototype);

    return CollectionEventAnnotationType;
  }

});
