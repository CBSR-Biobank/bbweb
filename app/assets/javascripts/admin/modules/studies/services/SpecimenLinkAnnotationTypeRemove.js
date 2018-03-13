/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/**
 *
 */
/* @ngInject */
function SpecimenLinkAnnotationTypeRemoveFactory(gettextCatalog,
                                                 annotationTypeRemove) {

  /**
   * An AngularJS factory used to remove {@link domain.studies.SpecimenLinkType SpecimenLinkType} {@link
   * domain.AnnotationType AnnotationTypes}.
   *
   * @memberOf admin.studies.services
   * @augments admin.common.services.AnnotationTypeRemove
   */
  class SpecimenLinkAnnotationTypeRemove extends annotationTypeRemove {
    constructor() {
      super(gettextCatalog.getString(`
This annotation type is in use by a specimen link type.
If you want to make changes to the annotation type,
it must first be removed from the specimen link type(s) that use it.`));
    }

  }

  return SpecimenLinkAnnotationTypeRemove;

}

export default ngModule => ngModule.service('SpecimenLinkAnnotationTypeRemove',
                                           SpecimenLinkAnnotationTypeRemoveFactory)
