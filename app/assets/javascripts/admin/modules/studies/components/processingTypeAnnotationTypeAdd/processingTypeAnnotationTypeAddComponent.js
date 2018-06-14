/**
 * AngularJS Component for {@link domain.studies.ProcessingType ProcessingType} administration.
 *
 * @namespace admin.studies.components.processingTypeAnnotationTypeAdd
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { AnnotationTypeAddController } from '../../../common/controllers/AnnotationTypeAddController'

/*
 * Controller for this component.
 */
class ProcessingTypeAnnotationTypeAddController extends AnnotationTypeAddController {

  constructor($state,
              notificationsService,
              domainNotificationService,
              modalService,
              breadcrumbService,
              gettextCatalog) {
    'ngInject';
    super($state,
          notificationsService,
          domainNotificationService,
          modalService,
          gettextCatalog,
          gettextCatalog.getString('Processing Type'),
          '^')

    Object.assign(this, { breadcrumbService })
  }

  $onInit() {
    const studySlug = this.study.slug,
          slug = this.processingType.slug

    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.studies'),
      this.breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.processing({ studySlug: "${studySlug}" })`,
        () => this.study.name),
      this.breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.processing.viewType({ studySlug: "${studySlug}", processingTypeSlug: "${slug}" })`,
        () => this.processingType.name),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.processing.viewType.annotationTypeView',
        () => this.gettextCatalog.getString('Add annotation'))
    ];
  }

  addAnnotationType(annotationType) {
    return this.processingType.addAnnotationType(annotationType)
  }
}

/**
 * An AngularJS component that allows the user to add an {@link domain.annotations.AnnotationType AnnotationType} to a
 * {@link domain.studies.ProcessingType ProcessingType}.
 *
 * @memberOf admin.studies.components.processingTypeAnnotationTypeAdd
 *
 * @param {domain.studies.Study} study - the study the *Collection Event Type* belongs to.
 *
 * @param {domain.studies.ProcessingType} processingType - the collection event type the
 * *Annotation Type* should be added to.
 */
const processingTypeAnnotationTypeAddComponent = {
  template: require('./processingTypeAnnotationTypeAdd.html'),
  controller: ProcessingTypeAnnotationTypeAddController,
  controllerAs: 'vm',
  bindings: {
    study:          '<',
    processingType: '<'
  }
};

function stateConfig($stateProvider, $urlRouterProvider) {
  'ngInject';
  $stateProvider.state('home.admin.studies.study.processing.viewType.annotationTypeAdd', {
    url: '/annottypes/add',
    views: {
        'main@': 'processingTypeAnnotationTypeAdd'
    }
  });
  $urlRouterProvider.otherwise('/');
}

export default ngModule => {
  ngModule
    .config(stateConfig)
    .component('processingTypeAnnotationTypeAdd', processingTypeAnnotationTypeAddComponent);
}
