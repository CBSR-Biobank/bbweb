/**
 * AngularJS Component for {@link domain.studies.CollectionEventType CollectionEventType} administration.
 *
 * @namespace admin.studies.components.collectionEventAnnotationTypeView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

/*
 * Controller for this component.
 */
/* @ngInject */
function CollectionEventAnnotationTypeViewController($q,
                                                     $state,
                                                     CollectionEventType,
                                                     gettextCatalog,
                                                     notificationsService,
                                                     breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //---

  function onInit() {
    const studySlug = vm.study.slug,
          slug = vm.collectionEventType.slug
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.studies'),
      breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.collection.ceventType({ studySlug: "${studySlug}", eventTypeSlug: "${slug}" })`,
        () => vm.study.name + ': ' + vm.collectionEventType.name),
      breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.collection.ceventType.annotationTypeView',
        () => {
          if (_.isUndefined(vm.annotationType)) {
            return gettextCatalog.getString('Error');
          }
          return gettextCatalog.getString('Event annotation: {{name}}',
                                          { name: vm.annotationType.name });
        })
    ];

    vm.onUpdate = onUpdate;

    // reload the collection event type in case changes were made to it
    CollectionEventType.get(vm.study.slug, vm.collectionEventType.slug)
      .then(function (ceventType) {
        vm.collectionEventType = ceventType;
      });
  }

  function onUpdate(attr, annotationType) {
    return vm.collectionEventType.updateAnnotationType(annotationType)
      .then(postUpdate)
      .catch(notificationsService.updateError)
      .then(notifySuccess)
      .then(() => {
        if (attr === 'name') {
          // reload the state so that the URL gets updated
          $state.go($state.current.name,
                    { annotationTypeSlug: vm.annotationType.slug },
                    { reload: true  })
        }
      });
  }

  function postUpdate(collectionEventType) {
    vm.collectionEventType = collectionEventType;
    vm.annotationType = _.find(vm.collectionEventType.annotationTypes, { id: vm.annotationType.id });
    if (_.isUndefined(vm.annotationType)) {
      return $q.reject('could not update annotation type');
    }

    return $q.when(true);
  }

  function notifySuccess() {
    return notificationsService.success(
      gettextCatalog.getString('Annotation type changed successfully.'),
      gettextCatalog.getString('Change successful'),
      1500);
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

export default ngModule => ngModule.component('collectionEventAnnotationTypeView',
                                             collectionEventAnnotationTypeViewComponent)
