/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
  'use strict';

  CollectionEventAnnotationTypeModalsFactory.$inject = [
    'gettextCatalog',
    'AnnotationTypeModals']
  ;

  /**
   *
   */
  function CollectionEventAnnotationTypeModalsFactory(gettextCatalog,
                                                      AnnotationTypeModals) {

    function CollectionEventAnnotationTypeModals() {
      AnnotationTypeModals.call(
        this,
        gettextCatalog.getString('This annotation type is in use by a collection event type. ' +
                'If you want to make changes to the annotation type, ' +
                'it must first be removed from the collection event type(s) that use it.'));
    }

    CollectionEventAnnotationTypeModals.prototype = Object.create(AnnotationTypeModals.prototype);
    CollectionEventAnnotationTypeModals.prototype.constructor = CollectionEventAnnotationTypeModals;

    return CollectionEventAnnotationTypeModals;

  }

  return CollectionEventAnnotationTypeModalsFactory;
});
