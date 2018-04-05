/**
 * AngularJS Component for {@link domain.studies.CollectionEventType CollectionEventType} administration.
 *
 * @namespace admin.studies.components.collectionEventAnnotationTypeAdd
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { AnnotationTypeAddController } from '../../../common/controllers/AnnotationTypeAddController'

/*
 * Controller for this component.
 */
class CollectionEventAnnotationTypeAddController extends AnnotationTypeAddController {

  constructor($state,
              notificationsService,
              domainNotificationService,
              modalService,
              breadcrumbService,
              gettextCatalog) {
    super($state,
          notificationsService,
          domainNotificationService,
          modalService,
          gettextCatalog,
          gettextCatalog.getString('Collection Event Type'),
          'home.admin.studies.study.collection.ceventType')

    Object.assign(this, { breadcrumbService })
  }

  $onInit() {
    const studySlug = this.study.slug,
          slug = this.collectionEventType.slug

    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.studies'),
      this.breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.collection.ceventType({ studySlug: "${studySlug}", eventTypeSlug: "${slug}" })`,
        () => this.study.name + ': ' + this.collectionEventType.name),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.collection.ceventType.annotationTypeView',
        () => this.gettextCatalog.getString('Add annotation'))
    ];
  }

  addAnnotationType(annotationType) {
    return this.collectionEventType.addAnnotationType(annotationType)
  }
}

/**
 * An AngularJS component that allows the user to add an {@link domain.annotations.AnnotationType AnnotationType} to a
 * {@link domain.studies.CollectionEventType CollectionEventType}.
 *
 * @memberOf admin.studies.components.collectionEventAnnotationTypeAdd
 *
 * @param {domain.studies.Study} study - the study the *Collection Event Type* belongs to.
 *
 * @param {domain.studies.CollectionEventType} collectionEventType - the collection event type the
 * *Annotation Type* should be added to.
 */
const collectionEventAnnotationTypeAddComponent = {
  template: require('./collectionEventAnnotationTypeAdd.html'),
  controller: CollectionEventAnnotationTypeAddController,
  controllerAs: 'vm',
  bindings: {
    study:               '<',
    collectionEventType: '<'
  }
};

export default ngModule => ngModule.component('collectionEventAnnotationTypeAdd',
                                             collectionEventAnnotationTypeAddComponent)
