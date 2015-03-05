define(['../module'], function(module) {
  'use strict';

  module.factory('SpecimenLinkAnnotationType', SpecimenLinkAnnotationTypeFactory);

  SpecimenLinkAnnotationTypeFactory.$inject = [
    'StudyAnnotationType',
    'spcLinkAnnotTypesService'
  ];

  /**
   *
   */
  function SpecimenLinkAnnotationTypeFactory(StudyAnnotationType,
                                             spcLinkAnnotTypesService) {

    function SpecimenLinkAnnotationType(obj) {
      obj = obj || {};
      StudyAnnotationType.call(this, obj);
      this._service = spcLinkAnnotTypesService;
    }

    SpecimenLinkAnnotationType.prototype = Object.create(StudyAnnotationType.prototype);

    return SpecimenLinkAnnotationType;
  }

});
