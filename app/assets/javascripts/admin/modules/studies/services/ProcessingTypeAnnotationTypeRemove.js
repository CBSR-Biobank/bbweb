/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * An AngularJS Factory
 */
/* @ngInject */
function ProcessingTypeAnnotationTypeRemoveFactory(gettextCatalog,
                                                    annotationTypeRemove) {

  /**
   * An AngularJS factory used to remove {@link domain.studies.ProcessingType ProcessingType} {@link
   * domain.annotations.AnnotationType AnnotationTypes}.
   *
   * @memberOf admin.studies.services
   * @augments admin.common.services.AnnotationTypeRemove
   */
  class ProcessingTypeAnnotationTypeRemove extends annotationTypeRemove {

    constructor() {
      super(gettextCatalog.getString(`
This annotation is in use by a processing step.
If you want to make changes to the annotation,
it must first be removed from the processing step that uses it.`));
    }
  }


  return ProcessingTypeAnnotationTypeRemove;

}

export default ngModule => ngModule.service('ProcessingTypeAnnotationTypeRemove',
                                           ProcessingTypeAnnotationTypeRemoveFactory)
