define(['../module'], function(module) {
  'use strict';

  module.factory('CollectionEventAnnotationType', CollectionEventAnnotationTypeFactory);

  CollectionEventAnnotationTypeFactory.$inject = [
    'StudyAnnotationType',
    'studyAnnotationTypeValidation',
    'ceventAnnotTypesService'
  ];

  /**
   *
   */
  function CollectionEventAnnotationTypeFactory(StudyAnnotationType,
                                                studyAnnotationTypeValidation,
                                                ceventAnnotTypesService) {

    function CollectionEventAnnotationType(obj) {
      obj = obj || {};
      StudyAnnotationType.call(this, obj);

      this._service = ceventAnnotTypesService;
    }

    CollectionEventAnnotationType.prototype = Object.create(StudyAnnotationType.prototype);

    CollectionEventAnnotationType.create = function(obj) {
      return new CollectionEventAnnotationType(obj);
    };

    CollectionEventAnnotationType.list = function(studyId) {
      return StudyAnnotationType.list(studyAnnotationTypeValidation.validateObj,
                                      CollectionEventAnnotationType.create,
                                      'ceannottypes',
                                      studyId);
    };

    CollectionEventAnnotationType.get = function(studyId, annotationTypeId) {
      return StudyAnnotationType.get(studyAnnotationTypeValidation.validateObj,
                                     CollectionEventAnnotationType.create,
                                      'ceannottypes',
                                     studyId,
                                     annotationTypeId);
    };

    CollectionEventAnnotationType.prototype.addOrUpdate = function () {
      return StudyAnnotationType.prototype.addOrUpdate.call(this).then(function (reply) {
        if (reply instanceof Error) {
          return reply;
        }
        return new CollectionEventAnnotationType(reply);
      });
    };

    return CollectionEventAnnotationType;
  }

});
