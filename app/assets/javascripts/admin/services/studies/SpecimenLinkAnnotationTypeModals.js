/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  SpecimenLinkAnnotationTypeModalsFactory.$inject = [
    'gettext',
    'AnnotationTypeModals'
  ];

  /**
   *
   */
  function SpecimenLinkAnnotationTypeModalsFactory(gettext,
                                                   AnnotationTypeModals) {

    function SpecimenLinkAnnotationTypeModals() {
      AnnotationTypeModals.call(
        this,
        gettext('This annotation type is in use by a specimen link type. ' +
                'If you want to make changes to the annotation type, ' +
                'it must first be removed from the specimen link type(s) that use it.'));
      }

    SpecimenLinkAnnotationTypeModals.prototype = Object.create(AnnotationTypeModals.prototype);
    SpecimenLinkAnnotationTypeModals.prototype.construcor = SpecimenLinkAnnotationTypeModals;

    return SpecimenLinkAnnotationTypeModals;

  }

  return SpecimenLinkAnnotationTypeModalsFactory;
});
