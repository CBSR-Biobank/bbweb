/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Angular factory.
 */
/* @ngInject */
function AnnotationTypeRemoveFactory(gettextCatalog,
                                     modalService,
                                     domainNotificationService) {

  /**
   * A base class that provides functions to create modals associated to removing {@link domain.annotations.AnnotationType
   * AnnotationTypes}.
   *
   * **This is an abstract base class and should should only be extended**. Instead use one of the following:
   *
   *   - {@link admin.studies.services.ParticipantAnnotationTypeRemove ParticipantAnnotationTypeRemove}
   *   - {@link admin.studies.services.CollectionEventAnnotationTypeRemove CollectionEventAnnotationTypeRemove}
   *   - {@link admin.studies.services.SpecimenLinkAnnotationTypeRemove SpecimenLinkAnnotationTypeRemove}
   *
   * @memberOf admin.common.services
   */
  class AnnotationTypeRemove {

    /**
     * @param {string} modalBodyHtml - a string, that may contain HTML tags, that is displayed in the modal's
     * body section when `removeInUseModal` is called.
     */
    constructor(modalBodyHtml) {
      this.modalBodyHtml = modalBodyHtml;
    }

    /**
     * Displays a modal with only an `OK` button stating that the {@link domain.annotations.AnnotationType
     * AnnotationType} cannot be removed.
     */
    removeInUseModal() {
      const headerHtml = gettextCatalog.getString('Cannot remove this annotation type');
      return modalService.modalOk(headerHtml, this.modalBodyHtml);
    }

    /**
     * Displays 2 modals, one after the other:
     *
     * - The first modal asks the user to confirm that they wish to delete an {@link domain.annotations.AnnotationType
     *   AnnotationType}.
     *
     * - The second modal is displayed if the user confirms the deletion and the server rejects the request to
     *   delete the {@link domain.annotations.AnnotationType AnnotationType}.
     *
     * @param {type} comment.
     */
    remove(annotationType, removePromiseFunc) {
      return domainNotificationService.removeEntity(
        removePromiseFunc,
        gettextCatalog.getString('Remove Annotation Type'),
        gettextCatalog.getString('Are you sure you want to remove annotation type {{name}}',
                                 { name: annotationType.name }),
        gettextCatalog.getString('Remove failed'),
        gettextCatalog.getString('Annotation type {{name}} cannot be removed',
                                 { name: annotationType.name }));
    }
  }

  return AnnotationTypeRemove;
}

export default ngModule => ngModule.factory('annotationTypeRemove', AnnotationTypeRemoveFactory)
