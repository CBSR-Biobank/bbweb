/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

var component = {
  template: require('./collectionEventAnnotationTypeView.html'),
  controller: CollectionEventAnnotationTypeViewController,
  controllerAs: 'vm',
  bindings: {
    study:               '<',
    collectionEventType: '<',
    annotationType:      '<'
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function CollectionEventAnnotationTypeViewController($q,
                                                     CollectionEventType,
                                                     gettextCatalog,
                                                     notificationsService,
                                                     breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //---

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.admin'),
      breadcrumbService.forState('home.admin.studies'),
      breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.collection.ceventType({ studyId: "${vm.collectionEventType.studyId}", ceventTypeId: "${vm.collectionEventType.id}" })`,
        () => vm.study.name),
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
    CollectionEventType.get(vm.collectionEventType.studyId, vm.collectionEventType.id)
      .then(function (ceventType) {
        vm.collectionEventType = ceventType;
      });
  }

  function onUpdate(annotationType) {
    return vm.collectionEventType.updateAnnotationType(annotationType)
      .then(postUpdate)
      .catch(notificationsService.updateError)
      .then(notifySuccess);
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

export default ngModule => ngModule.component('collectionEventAnnotationTypeView', component)
