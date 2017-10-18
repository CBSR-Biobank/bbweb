/**
 *
 */

import _ from 'lodash'

var component = {
  template: require('./participantAnnotationTypeView.html'),
  controller: ParticipantAnnotationTypeViewController,
  controllerAs: 'vm',
  bindings: {
    study:          '<',
    annotationType: '<'
  }
};

/*
 * Controller for this component.
 */
/* @ngInject */
function ParticipantAnnotationTypeViewController($q,
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
        'home.admin.studies.study.participants',
        function () { return vm.study.name; }),
      breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.participant.annotationTypeView',
        function () {
          if (_.isUndefined(vm.annotationType)) {
            return gettextCatalog.getString('Error');
          }
          return gettextCatalog.getString(
            'Participant annotation: {{name}}', { name: vm.annotationType.name });
        })
    ];

    vm.onUpdate = onUpdate;
  }

  function onUpdate(annotationType) {
    return vm.study.updateAnnotationType(annotationType)
      .then(postUpdate)
      .then(notifySuccess)
      .catch(notificationsService.updateError);
  }

  function postUpdate(study) {
    vm.study = study;
    vm.annotationType = _.find(vm.study.annotationTypes, { id: vm.annotationType.id });
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

export default ngModule => ngModule.component('participantAnnotationTypeView', component)
