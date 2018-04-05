/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Angular factory.
 */

/*
 * An AngularJS Factory
 */
/* @ngInject */
function ParticipAntannotationTypeRemoveFactory(gettextCatalog,
                                                annotationTypeRemove) {

  /**
   * An AngularJS factory used to remove {@link domain.collection.Participant Participant} {@link
   * domain.annotations.AnnotationType AnnotationTypes}.
   *
   * @memberOf admin.studies.services
   * @augments admin.common.services.AnnotationTypeRemove
   */
  class ParticipantAnnotationTypeRemove extends annotationTypeRemove {

    constructor() {
      super(gettextCatalog.getString(`
This annotation type is in use by participants.
If you want to make changes to the annotation type,
it must first be removed from the participants that use it.`));
    }

  }

  return ParticipantAnnotationTypeRemove;

}

export default ngModule => ngModule.factory('ParticipantAnnotationTypeRemove',
                                           ParticipAntannotationTypeRemoveFactory)
