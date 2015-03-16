define(['../module'], function(module) {
  'use strict';

  module.factory('SpecimenLinkAnnotationType', SpecimenLinkAnnotationTypeFactory);

  SpecimenLinkAnnotationTypeFactory.$inject = [
    'StudyAnnotationType',
    'studyAnnotationTypeValidation',
    'spcLinkAnnotTypesService'
  ];

  /**
   *
   */
  function SpecimenLinkAnnotationTypeFactory(StudyAnnotationType,
                                             studyAnnotationTypeValidation,
                                             spcLinkAnnotTypesService) {

    function SpecimenLinkAnnotationType(obj) {
      obj = obj || {};
      StudyAnnotationType.call(this, obj);

      this._service = spcLinkAnnotTypesService;
    }

    SpecimenLinkAnnotationType.prototype = Object.create(StudyAnnotationType.prototype);

    SpecimenLinkAnnotationType.create = function(obj) {
      return new SpecimenLinkAnnotationType(obj);
    };

    SpecimenLinkAnnotationType.list = function(studyId) {
      return StudyAnnotationType.list(studyAnnotationTypeValidation.validateObj,
                                      SpecimenLinkAnnotationType.create,
                                      'slannottypes',
                                      studyId);
    };

    SpecimenLinkAnnotationType.get = function(studyId, annotationTypeId) {
      return StudyAnnotationType.get(studyAnnotationTypeValidation.validateObj,
                                     SpecimenLinkAnnotationType.create,
                                     'slannottypes',
                                     studyId,
                                     annotationTypeId);
    };

    SpecimenLinkAnnotationType.prototype.addOrUpdate = function () {
      return StudyAnnotationType.prototype
        .addOrUpdate.call(this,
                          studyAnnotationTypeValidation.validateObj,
                          SpecimenLinkAnnotationType.create);
    };

    return SpecimenLinkAnnotationType;
  }

});
