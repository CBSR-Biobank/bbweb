/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * An AngularJS Factory
 */
/* @ngInject */
function CollectionEventAnnotationTypeRemoveFactory(gettextCatalog,
                                                    annotationTypeRemove) {

  /**
   * An AngularJS factory used to remove {@link domain.studies.CollectionEventType CollectionEventType} {@link
   * domain.AnnotationType AnnotationTypes}.
   *
   * @memberOf admin.studies.services
   * @augments admin.common.services.AnnotationTypeRemove
   */
  class CollectionEventAnnotationTypeRemove extends annotationTypeRemove {

    constructor() {
      super(gettextCatalog.getString(`
This annotation type is in use by a collection event type.
If you want to make changes to the annotation type,
it must first be removed from the collection event type(s) that use it.`));
    }
  }


  return CollectionEventAnnotationTypeRemove;

}

export default ngModule => ngModule.service('CollectionEventAnnotationTypeRemove',
                                           CollectionEventAnnotationTypeRemoveFactory)
