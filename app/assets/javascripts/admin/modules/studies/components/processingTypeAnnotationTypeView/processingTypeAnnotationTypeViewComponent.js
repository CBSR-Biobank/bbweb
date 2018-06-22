/**
 * AngularJS Component for {@link domain.studies.ProcessingType ProcessingType} administration.
 *
 * @namespace admin.studies.components.processingTypeAnnotationTypeView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'
import angular from 'angular';

class ProcessingTypeAnnotationTypeViewController {

  constructor($q,
              $state,
              ProcessingType,
              ProcessingTypeAnnotationTypeRemove,
              gettextCatalog,
              notificationsService,
              breadcrumbService) {
    'ngInject';
    Object.assign(this,
                  {
                    $q,
                    $state,
                    ProcessingType,
                    gettextCatalog,
                    notificationsService,
                    breadcrumbService
                  });
    this.annotationTypeRemove = new ProcessingTypeAnnotationTypeRemove();
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
        () => {
          if (_.isUndefined(this.annotationType)) {
            return this.gettextCatalog.getString('Error');
          }
          return this.gettextCatalog.getString('Annotation: {{name}}', { name: this.annotationType.name });
        })
    ];

    // reload the processing type in case changes were made to it
    this.ProcessingType.get(this.study.slug, this.processingType.slug)
      .then(processingType => {
        this.processingType = processingType;
      });
  }

  onUpdate(attr, annotationType) {
    return this.processingType.updateAnnotationType(annotationType)
      .then(processingType => {
        this.processingType = processingType;
        this.annotationType = _.find(this.processingType.annotationTypes,
                                     { id: this.annotationType.id });
        if (_.isUndefined(this.annotationType)) {
          return this.$q.reject('could not update annotation type');
        }

        return this.$q.when(true);
      })
      .catch(error => {
        this.notificationsService.updateError(error);
      })
      .then(() => this.notificationsService.success(
        this.gettextCatalog.getString('Annotation type changed successfully.'),
        this.gettextCatalog.getString('Change successful'),
        1500))
      .then(() => {
        if (attr === 'name') {
          // reload the state so that the URL gets updated
          this.$state.go(this.$state.current.name,
                         { annotationTypeSlug: this.annotationType.slug },
                         { reload: true  })
        }
      });
  }

  removeRequest() {
    if (!this.study.isDisabled()) {
      throw new Error('modifications not allowed');
    }

    this.annotationTypeRemove.remove(
      this.annotationType,
      () => this.processingType.removeAnnotationType(this.annotationType))
      .then(() => {
        this.notificationsService.success(this.gettextCatalog.getString('Annotation removed'));
        this.$state.go('^', {}, { reload: true });
      })
      .catch(angular.noop);
}

}

/**
 * An AngularJS component that allows the user to display an {@link domain.annotations.AnnotationType AnnotationType} that
 * belongs to a {@link domain.studies.ProcessingType ProcessingType}.
 *
 * @memberOf admin.studies.components.processingTypeAnnotationTypeView
 *
 * @param {domain.studies.Study} study - the study the collection event type belongs to.
 *
 * @param {domain.studies.ProcessingType} processingType - the collection event type the
 * annotation type belongs to.
 *
 * @param {domain.annotations.AnnotationType} annotationType - the annotation type to display.
 */
const processingTypeAnnotationTypeViewComponent = {
  template: require('./processingTypeAnnotationTypeView.html'),
  controller: ProcessingTypeAnnotationTypeViewController,
  controllerAs: 'vm',
  bindings: {
    study:          '<',
    processingType: '<',
    annotationType: '<'
  }
};

function resolveAnnotationType($q, $transition$, processingType, resourceErrorService) {
  'ngInject';
  const slug = $transition$.params().annotationTypeSlug,
        annotationType = _.find(processingType.annotationTypes, { slug }),
        result = annotationType ? $q.when(annotationType) : $q.reject('invalid annotation type ID')
  return result.catch(resourceErrorService.goto404(`invalid processing-type annotation-type ID: ${slug}`))
}

function stateConfig($stateProvider, $urlRouterProvider) {
  'ngInject';
  $stateProvider
    .state('home.admin.studies.study.processing.viewType.annotationTypeView', {
      url: '/annottypes/{annotationTypeSlug}',
      resolve: {
        annotationType: resolveAnnotationType
      },
      views: {
        'main@': 'processingTypeAnnotationTypeView'
      }
    });
  $urlRouterProvider.otherwise('/');
}

export default ngModule => {
  ngModule
    .config(stateConfig)
    .component('processingTypeAnnotationTypeView', processingTypeAnnotationTypeViewComponent);
}
