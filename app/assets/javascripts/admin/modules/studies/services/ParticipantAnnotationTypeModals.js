/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */

/**
 *
 */
/* @ngInject */
function ParticipantAnnotationTypeModalsFactory(gettextCatalog,
                                                AnnotationTypeModals) {

  function ParticipantAnnotationTypeModals() {
    AnnotationTypeModals.call(
      this,
      gettextCatalog.getString('This annotation type is in use by participants. ' +
                               'If you want to make changes to the annotation type, ' +
                               'it must first be removed from the participants that use it.'));
  }

  ParticipantAnnotationTypeModals.prototype = Object.create(AnnotationTypeModals.prototype);
  ParticipantAnnotationTypeModals.prototype.constructor = ParticipantAnnotationTypeModals.prototype;

  return ParticipantAnnotationTypeModals;

}

export default ngModule => ngModule.service('ParticipantAnnotationTypeModals',
                                           ParticipantAnnotationTypeModalsFactory)
