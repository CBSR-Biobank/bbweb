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
   * domain.annotations.AnnotationType AnnotationTypes}.
   *
   * @memberOf admin.studies.services
   * @augments admin.common.services.AnnotationTypeRemove
   */
  class CollectionEventAnnotationTypeRemove extends annotationTypeRemove {

    constructor() {
      super(gettextCatalog.getString(`
This annotation is in use by a collection event.
If you want to make changes to the annotation,
it must first be removed from the collection event that uses it.`));
    }
  }


  return CollectionEventAnnotationTypeRemove;

}

export default ngModule => ngModule.service('CollectionEventAnnotationTypeRemove',
                                           CollectionEventAnnotationTypeRemoveFactory)
