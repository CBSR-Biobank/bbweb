/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import { AnnotationTypeAddController } from '../../../common/controllers/AnnotationTypeAddController'

/*
 * Controller for this component.
 */
class Controller extends AnnotationTypeAddController {

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

const component = {
  template: require('./collectionEventAnnotationTypeAdd.html'),
  controller: Controller,
  controllerAs: 'vm',
  bindings: {
    study:               '<',
    collectionEventType: '<'
  }
};

export default ngModule => ngModule.component('collectionEventAnnotationTypeAdd', component)
