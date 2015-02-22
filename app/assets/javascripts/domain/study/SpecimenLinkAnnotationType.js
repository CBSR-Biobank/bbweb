define(['../module'], function(module) {
  'use strict';

  module.factory('SpecimenLinkAnnotationType', SpecimenLinkAnnotationTypeFactory);

  SpecimenLinkAnnotationTypeFactory.$inject = ['StudyAnnotationType'];

  /**
   *
   */
  function SpecimenLinkAnnotationTypeFactory(StudyAnnotationType) {

    function SpecimenLinkAnnotationType(obj) {
      obj = obj || {};
      StudyAnnotationType.call(this, obj);
    }

    SpecimenLinkAnnotationType.prototype = Object.create(StudyAnnotationType.prototype);

    return SpecimenLinkAnnotationType;
  }

});
