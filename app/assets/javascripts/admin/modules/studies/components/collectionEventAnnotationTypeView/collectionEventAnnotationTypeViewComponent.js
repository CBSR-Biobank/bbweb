/**
 * AngularJS Component for {@link domain.studies.CollectionEventType CollectionEventType} administration.
 *
 * @namespace admin.studies.components.collectionEventAnnotationTypeView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'
import angular from 'angular';

class CollectionEventAnnotationTypeViewController {

  constructor($q,
              $state,
              CollectionEventType,
              CollectionEventAnnotationTypeRemove,
              gettextCatalog,
              notificationsService,
              breadcrumbService) {
    'ngInject';
    Object.assign(this,
                  {
                    $q,
                    $state,
                    CollectionEventType,
                    gettextCatalog,
                    notificationsService,
                    breadcrumbService
                  });
    this.annotationTypeRemove = new CollectionEventAnnotationTypeRemove();
  }

  $onInit() {
    const studySlug = this.study.slug,
          slug = this.collectionEventType.slug
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.studies'),
      this.breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.collection({ studySlug: "${studySlug}" })`,
        () => this.study.name),
      this.breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.collection.ceventType({ studySlug: "${studySlug}", eventTypeSlug: "${slug}" })`,
        () => this.collectionEventType.name),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.collection.ceventType.annotationTypeView',
        () => {
          if (_.isUndefined(this.annotationType)) {
            return this.gettextCatalog.getString('Error');
          }
          return this.gettextCatalog.getString('Annotation: {{name}}', { name: this.annotationType.name });
        })
    ];

    // reload the collection event type in case changes were made to it
    this.CollectionEventType.get(this.study.slug, this.collectionEventType.slug)
      .then(ceventType => {
        this.collectionEventType = ceventType;
      });
  }

  onUpdate(attr, annotationType) {
    return this.collectionEventType.updateAnnotationType(annotationType)
      .then(collectionEventType => {
        this.collectionEventType = collectionEventType;
        this.annotationType = _.find(this.collectionEventType.annotationTypes,
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
      () =>
        this.collectionEventType.removeAnnotationType(this.annotationType))
      .then(() => {
        this.notificationsService.success(this.gettextCatalog.getString('Annotation removed'));
        this.$state.go('^', {}, { reload: true });
      })
      .catch(angular.noop);
  }

}

/**
 * An AngularJS component that allows the user to display an {@link domain.annotations.AnnotationType AnnotationType} that
 * belongs to a {@link domain.studies.CollectionEventType CollectionEventType}.
 *
 * @memberOf admin.studies.components.collectionEventAnnotationTypeView
 *
 * @param {domain.studies.Study} study - the study the collection event type belongs to.
 *
 * @param {domain.studies.CollectionEventType} collectionEventType - the collection event type the
 * annotation type belongs to.
 *
 * @param {domain.annotations.AnnotationType} annotationType - the annotation type to display.
 */
const collectionEventAnnotationTypeViewComponent = {
  template: require('./collectionEventAnnotationTypeView.html'),
  controller: CollectionEventAnnotationTypeViewController,
  controllerAs: 'vm',
  bindings: {
    study:               '<',
    collectionEventType: '<',
    annotationType:      '<'
  }
};

function resolveAnnotationType($q, $transition$, collectionEventType, resourceErrorService) {
  'ngInject';
  const slug = $transition$.params().annotationTypeSlug,
        annotationType = _.find(collectionEventType.annotationTypes, { slug  }),
        result = annotationType ? $q.when(annotationType) : $q.reject('invalid annotation type ID')
  return result.catch(resourceErrorService.goto404(`invalid event-type annotation-type ID: ${slug}`))
}

function stateConfig($stateProvider, $urlRouterProvider) {
  'ngInject';
  $stateProvider
    .state('home.admin.studies.study.collection.ceventType.annotationTypeView', {
      url: '/annottypes/{annotationTypeSlug}',
      resolve: {
        annotationType: resolveAnnotationType
      },
      views: {
        'main@': 'collectionEventAnnotationTypeView'
      }
    });
  $urlRouterProvider.otherwise('/');
}

export default ngModule => {
  ngModule
    .config(stateConfig)
    .component('collectionEventAnnotationTypeView', collectionEventAnnotationTypeViewComponent);
}
