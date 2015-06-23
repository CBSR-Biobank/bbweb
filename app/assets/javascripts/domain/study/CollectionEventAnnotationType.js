/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  CollectionEventAnnotationTypeFactory.$inject = [
    'StudyAnnotationType',
    'studyAnnotationTypeValidation',
    'ceventAnnotationTypesService'
  ];

  /**
   *
   */
  function CollectionEventAnnotationTypeFactory(StudyAnnotationType,
                                                studyAnnotationTypeValidation,
                                                ceventAnnotationTypesService) {

    function CollectionEventAnnotationType(obj) {
      obj = obj || {};
      StudyAnnotationType.call(this, obj);

      this._service = ceventAnnotationTypesService;
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
      return StudyAnnotationType.prototype
        .addOrUpdate.call(this,
                          studyAnnotationTypeValidation.validateObj,
                          CollectionEventAnnotationType.create);
    };

    return CollectionEventAnnotationType;
  }

  return CollectionEventAnnotationTypeFactory;
});
